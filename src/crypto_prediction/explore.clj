(ns crypto-prediction.explore
  (:require [taoensso.timbre :as log]
            [org.purefn.sentenza.annotate :as sann]
            [com.stuartsierra.component :as component]
            [com.stuartsierra.component.repl :as repl :refer [set-init start stop system]]


            [cortex.util :as util]
            [com.stuartsierra.frequencies :as freq]

            [crypto-prediction.process :as p]
            [crypto-prediction.visualize :as v]
            [crypto-prediction.utils :as u]))


;; ANOTHER FUN CASE STUDY
;; NUMBERICAL ANALYSIS
(defn stats
  "Summarize sequences of features across all crypto
  currencies. Returns map keyed on symbol with summary for each
  feature."
  [data & features]
  (->> data
       (map p/transform)
       (group-by :symbol)
       (map (fn [[sym coll]]
              {sym (reduce (fn [m kw]
                             (assoc m kw
                                    (-> (map kw coll)
                                        (frequencies)
                                        (freq/stats))))
                           {} features)}))
       (into {})))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Execution time mean : 16.653979 sec                   ;;
;; Execution time std-deviation : 667.958547 ms          ;;
;; Execution time lower quantile : 16.190020 sec ( 2.5%) ;;
;; Execution time upper quantile : 17.583579 sec (97.5%) ;;
;; Overhead used : 1.683132 ns                           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn stats-xf
  [data & features]
  (into {}
        (comp (map p/transform)
              (u/grouped-by :symbol)
              (map (fn [group]
                     (let [[k v] (first group)]
                       {k (reduce
                           (fn [m kw]
                             (assoc m kw
                                    (-> (map kw v)
                                        (frequencies)
                                        (freq/stats))))
                           {} features)})))) data))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Execution time mean : 13.636207 sec                   ;;
;; Execution time std-deviation : 107.188629 ms          ;;
;; Execution time lower quantile : 13.450868 sec ( 2.5%) ;;
;; Execution time upper quantile : 13.736036 sec (97.5%) ;;
;; Overhead used : 1.683132 ns                           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn stats-cored
  [data & features]
  (let [p1 (u/cored data
                    [(-> (map p/transform)
                         (sann/cored 4))])]
    (into {} (u/cored (map identity (group-by :symbol p1))
                      [(-> (comp (u/grouped-by :symbol)
                                 (map (fn [group]
                                        (let [[k v] group]
                                          {k (reduce
                                              (fn [m kw]
                                                (assoc m kw
                                                       (-> (map kw v)
                                                           (frequencies)
                                                           (freq/stats))))
                                              {} features)}))))
                          (sann/cored 4))]))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Execution time mean : 13.159651 sec                   ;;
;; Execution time std-deviation : 987.310492 ms          ;;
;; Execution time lower quantile : 12.279480 sec ( 2.5%) ;;
;; Execution time upper quantile : 14.792551 sec (97.5%) ;;
;; Overhead used : 1.710295 ns                           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; CONCLUSION:
;; Same as above with results amplified due to heavier computation.



;; CORTEX FUN
;; https://functional.works-hub.com/learn/Learning-Machine-Learning-With-Clojure-and-Cortex





;; Exploration
;; https://www.kaggle.com/arsenalist/bitcoin-prices-prediction
