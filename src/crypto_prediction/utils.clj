(ns crypto-prediction.utils
  (:require [clojure.java.io :as jio]
            [clojure.string :as cstr]
            [taoensso.timbre :as log]
            [org.purefn.sentenza.api :as sz]
            [org.purefn.sentenza.annotate :as sann]


            [clojure.core.async :as async :refer [>! <! go-loop chan close! <!!]])
  (:import (java.io BufferedReader FileReader FileInputStream BufferedInputStream InputStreamReader)))

(defn cored [records xfs]
  (-> (sz/kickoff-flow records
                       xfs
                       :collect true)
      (last)
      (deref)))

(defn read-csv-header
  [filename]
  (with-open [r (jio/reader (jio/resource filename))]
    (let [[header & lines] (line-seq r)
          headers          (->> (cstr/split header #",")
                                (map (comp keyword #(cstr/replace % #"_" "-"))))]
      (into []
            (comp (map #(cstr/split % #","))
                  (map (partial zipmap headers)))
            lines))))

(defn sparsity [records]
  (/ (count (filter (comp seq (partial filter nil?)) records))
     (count records)))

(defn grouped-by
  "A transducer that acts like group-by but includes the result as a single result in the stream.
   Options:
   :keys? false
     (grouped-by f :keys? false) is like (vals (group-by f coll))
   :extract fn
     (grouped-by f :extract extract) is like this library's (group-by-extract f extract coll).
   "
  [f & {:keys [keys? extract] :or {keys? true}}]
  (fn [rf]
    (let [group (volatile! (transient (array-map)))]
      (fn
        ([] (rf))
        ([result]
         (rf
          (if keys?
            (rf result (persistent! @group))
            (rf result (vals (persistent! @group))))))
        ([result x]
         (vswap! group (fn [g]
                         (let [k (f x)
                               x (if extract (extract x) x)]
                           (if-let [v (get g k)]
                             (assoc! g k (conj v x))
                             (assoc! g k [x])))))
         result)))))




;; Thanks to atroche
;; Case study for understanding multi threaded file reading.
;; First objective... load the file QUICKLY
;; https://hackernoon.com/counting-lines-60-faster-than-wc-with-clojure-core-async-1af4ce058884
(def one-meg (* 1024 1024))

(defn ^FileInputStream input-stream
  [^String fname]
  (FileInputStream. fname))

(defn count-newlines
  [^bytes barray]
  (let [num-bytes (alength barray)]
    (loop [i        0
           newlines 0]
      (if (>= i num-bytes)
        newlines
        (if (= 10 (aget ^bytes barray i))
          (recur (inc i)
                 (inc newlines))
          (recur (inc i)
                 newlines))))))

(defn count-file
  [filename]
  (with-open [file-stream (FileInputStream. filename)]
    (let [channel  (chan 500)
          counters (for [_ (range 4)]
                     (go-loop [newline-count 0]
                       (let [barray (async/<! channel)]
                         (if (nil? barray)
                           newline-count
                           (recur (+ newline-count
                                     (count-newlines barray)))))))]
      (go-loop []
        (let [barray     (byte-array one-meg)
              bytes-read (.read file-stream barray)]
          ;; This will block if too much data is waiting in channel
          (>! channel barray)
          ;; .read returns a -1 on EOF
          (if (> bytes-read 0)
            (recur)
            (close! channel))))
      (reduce + (map <!! counters)))))
