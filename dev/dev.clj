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
            [oz.core :as oz]
            [criterium.core :as ct]

            [crypto-prediction.data :as d]
            [crypto-prediction.process :as p]
            [crypto-prediction.explore :as e]
            [crypto-prediction.visualize :as v]
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
