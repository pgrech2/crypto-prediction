(ns crypto-prediction.visualize
  (:require [oz.core :as oz]))

;; Visualizations
;; https://vega.github.io/vega-lite/docs/

;; Vega Example
(defn group-data [& names]
  (mapcat #(map (fn [i x]
                  {:x (inc i) :y x :col %})
                (range)
                (take 5 (repeatedly (partial rand-int 100))))
          names))

(defn line-plot [sym records]
  {:data {:values (->> records
                       (filter (comp (partial = sym) :col))
                       (take-nth 5))}
   :encoding {:x {:timeUnit "yearmonthdate"
                  :field "x"}
              :y {:field "y"}
              :color {:field "col" :type "nominal"}}
   :mark {:type "line"
          :interpolate "step-after"}
   :width "1000"
   :height "1000"})

(defn plot [data]
  (oz/v! (line-plot data "BTC-open")))
