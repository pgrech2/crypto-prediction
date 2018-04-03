(ns crypto-prediction.model
  (:require [cortex.experiment.train :as train]
            [cortex.nn.execute :as execute]
            [cortex.nn.layers :as layers]
            [cortex.nn.network :as network]

            [crypto-prediction.process :as p]))


;; Networks and layers namespaces to be used to define internals of
;; network.

;; Train namespace takes network definition and datasets to produce a
;; trained network.

;; Execute namespace takes trained network and an extra input-only
;; dataset to run the network with the provided input.


(defn train-set [data split]
  (first (split-at (* (count data) split) data)))

(defn test-set [data split]
  (last (split-at (* (count data) split) data)))



;; Common linear network made of four layers
;; Input layer with `id` open
;; Two layers will define internal structure (black magic!)
;; Output layer with `id` open-pred
(def network
  (network/linear-network
   [(layers/input 2 1 1 :id :x)
    (layers/linear->tanh 10)
    (layers/tanh)
    (layers/linear 1 :id :y)]))
