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
