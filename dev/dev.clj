(ns dev
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [clojure.tools.namespace.repl :refer [refresh refresh-all clear]]

            [com.stuartsierra.component :as component]
            [com.stuartsierra.component.repl :as repl :refer [set-init start stop system reset]]
            [com.stuartsierra.frequencies :as freq]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [org.purefn.sentenza.annotate :as sann]

            [cortex.util :as util]
            [cortex.experiment.train :as train]
            [cortex.nn.execute :as execute]
            [cortex.nn.layers :as layers]
            [cortex.nn.network :as network]
            [oz.core :as oz]
            [criterium.core :as ct]

            [crypto-prediction.data :as d]
            [crypto-prediction.process :as p]
            [crypto-prediction.explore :as e]
            [crypto-prediction.visualize :as v]
            [crypto-prediction.model :as m]
            [crypto-prediction.utils :as u]))


(defn dev-system
  []
  (component/system-map
   :data (d/loader {:filename "crypto-markets.csv"})))

(set-init (fn [_] (dev-system)))

(defn all-data [sys]
  (d/records (:data sys)))

(defn tfm-data [sys]
  (map p/transform (all-data sys)))

;; DEVELOPMENT
(def train-set (-> (all-data system)
                    (p/extract "BTC")
                    (->> (map p/network-transform)
                         (sort-by :timestamp))
                    (m/train-set 0.7)))
(def test-set (-> (all-data system)
                   (p/extract "BTC")
                   (->> (map p/network-transform)
                        (sort-by :timestamp))
                   (m/test-set 0.7)))

(def trained  (binding [*out* (clojure.java.io/writer "my-training.log")]
                (train/train-n m/network train-set test-set
                               :batch-size 100
                               :network-filestem "my-fn"
                               :epoch-count 3000)))
;; (execute/run trained [{:x [0 1]}])
