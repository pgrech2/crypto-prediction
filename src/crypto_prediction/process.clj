(ns crypto-prediction.process
  (:require [taoensso.timbre :as log]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [com.stuartsierra.component :as component]
            [com.stuartsierra.component.repl :as repl :refer [set-init start stop system]]
            [org.purefn.sentenza.annotate :as sann]

            [cortex.util :as util]
            [com.stuartsierra.frequencies :as freq]
            [crypto-prediction.utils :as u]))


;; Example
;; :date "2013-04-28"

;; :name "Bitcoin"
;; :symbol "BTC"
;; :slug "bitcoin"

;; :open "135.3"
;; :close "134.21"

;; :high "135.98"
;; :low "132.1"
;; :spread "3.88"

;; :volume "0"
;; :ranknow "1"
;; :market "1500520000"
;; :close_ratio "0.5438"

(defn transform [record]
  (let [{:keys [open
                date
                slug
                spread
                symbol
                name
                close
                volume
                high
                ranknow
                low
                market
                close_ratio]} record
        date-time (coerce/from-string date)]
    (-> record
        (select-keys [:symbol :ranknow])
        ;; Date features
        (assoc :year (time/year date-time)
               :month (time/month date-time)
               :day-of-month (time/day date-time)
               :day-of-week (time/day-of-week date-time)
               :timestamp (coerce/to-epoch date-time))

        ;; Numberical features
        (assoc :ranknow (Integer. ranknow)
               :open (Float/valueOf open)
               :close (Float/valueOf close)
               :high (Float/valueOf high)
               :low (Float/valueOf low)
               :spread (Float/valueOf spread)))))

(defn network-transform
  [record]
  (let [{:keys [open
                close
                high
                low
                spread
                timestamp]} record]
    {:timestamp timestamp
     :x [high low
         ;;close spread
         ]
     :y [open]}))


(defn vis-transform
  "Transform function for visualizations"
  [record symbol feature]
  (let [{:keys [date
                open
                close
                spread]} record]
    (-> record
        (assoc :x date
               :y (Float/valueOf (get record feature))
               :col (str symbol "-" "open"))
        (select-keys [:x :y :col]))))

(def features [:open :close :high :low :spread :timestamp])

;; SYMBOL EXTRACTION AND PROCESSING
;; CASE STUDY ON LAZY SEQUENCES VS TRANSDUCERS VS CORED
(defn extract
  [data symbol]
  (->> data
       (filter (comp (partial = symbol) :symbol))
       (map transform)
       (map #(select-keys % features))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Execution time mean : 330.607489 ms                   ;;
;; Execution time std-deviation : 21.307986 ms           ;;
;; Execution time lower quantile : 293.642987 ms ( 2.5%) ;;
;; Execution time upper quantile : 346.473129 ms (97.5%) ;;
;; Overhead used : 1.988125 ns                           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn extract-xf
  [data symbol]
  (into [] (comp (filter (comp (partial = symbol) :symbol))
                 (map transform)
                 (map #(select-keys % features)))
        data))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Execution time mean : 316.266419 ms                   ;;
;; Execution time std-deviation : 18.790580 ms           ;;
;; Execution time lower quantile : 296.989532 ms ( 2.5%) ;;
;; Execution time upper quantile : 340.512640 ms (97.5%) ;;
;; Overhead used : 1.988125 ns                           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn extract-core
  [data symbol]
  (u/cored data
           [(-> (comp (filter (comp (partial = symbol) :symbol))
                      (map transform)
                      (map #(select-keys % features)))
                (sann/cored 4))]))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Execution time mean : 3.117743 sec                   ;;
;; Execution time std-deviation : 82.613040 ms          ;;
;; Execution time lower quantile : 3.027100 sec ( 2.5%) ;;
;; Execution time upper quantile : 3.229354 sec (97.5%) ;;
;; Overhead used : 1.988125 ns                          ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; CONCLUSION: Best performance from lazy-seq for development (when
;; entire sequence is not realized). However when processing of entire
;; data set, performance improvement continues to grow through the use of
;; transducers. Cored computations should be saved for when there is
;; significant analysis on LARGE data sets due to overhead.
