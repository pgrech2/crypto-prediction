(ns crypto-prediction.model
  (:require [cortex.experiment.train :as train]
            [cortex.nn.execute :as execute]
            [cortex.nn.layers :as layers]
            [cortex.nn.network :as network]

            [crypto-prediction.process :as p]))

;; Different splitting strategies can differ significatly
;; 1. in generated features
;; 2. in a way the model will rely on that features
;; 3. in some kind of target leak

;; Need to set up validation to mimic train/test split by organizers
;; CATEGORIES
;; 1. Random  / rowwise (for independent data)
;; 2. Timewise - everything before is training, everything after is test
;; 2a. Moving window validation
;; 3. By id - based on characteristic of record
;; 4. Combined - split for each category independently

;; 1. In most cases data is split by
;; 1a. row number
;; 1b. Time
;; 1c. Id
;; 2. Logic of feature generaiton depends on the data splitting strategy
;; 3. Set up your validation to mimic the train/test split of competition







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




;; LAYERS - activation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 1. linear - linear                            ;;
;; 2. step - ?                                   ;;
;; 3. Sigmoid - logistic                         ;;
;; 4. Tanh - tanh                                ;;
;; 5. Rectified linear unit ReLu function - relu ;;
;; 6. Parametric ReLu function - prelu           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;




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




(defn train
  [network train-data test-data out-file]
  (binding [*out* (clojure.java.io/writer (str out-file "-training.log"))]
    (future (train/train-n network train-data test-data
                          :batch-size 100
                          :network-filestem out-file
                          :epoch-count 3000))
    nil))

;; (execute/run trained [{:x [0 1]}])

(defn epoch-loss
  [network]
  (select-keys network [:epoch-count :cv-loss]))
