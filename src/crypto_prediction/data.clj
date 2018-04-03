(ns crypto-prediction.data
  (:require [clojure.java.io :as jio]
            [clojure.string :as cstr]
            [taoensso.timbre :as log]
            [com.stuartsierra.component :as component]

            [org.purefn.sentenza.annotate :as sann]
            [crypto-prediction.data.proto :as p]
            [crypto-prediction.utils :as u]))

(defrecord Loader [config source]

  component/Lifecycle
  (start [this]
    (log/info "Starting Extractor")
    (if source
      (do (log/info "Data already loaded...")
          this)
      (do (log/info "Loading data from" (:filename config))
          (assoc this :source (-> (:filename config)
                                  (u/read-csv-header))))))

  (stop [this]
    (log/info "Stopping Extractor")
    (if source
      (assoc this :source nil)
      (do (log/info "No data loaded...")
          this)))

  p/Loader
  (records [this]
    source))


(defn loader
  [config]
  (map->Loader {:config config}))

(defn records [data]
  (p/records data))
