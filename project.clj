(defproject crypto-prediction "0.1.0-SNAPSHOT"
  :description "Demonstration of using thinktopic/cortex neural networks to predict crypto currency prices."
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [com.stuartsierra/component "0.3.2"]
                 [com.taoensso/timbre "4.7.4"]
                 [clj-time "0.13.0"]

                 [thinktopic/cortex "0.9.12"]
                 [thinktopic/experiment "0.9.12"]
                 [metasoarous/oz "1.3.1"]
                 [com.stuartsierra/frequencies "0.1.0"]
                 [org.purefn/sentenza "0.1.8"]

                 [criterium "0.4.4"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [com.stuartsierra/component.repl "0.2.0"]]
                   :jvm-opts ["-Xmx2g"]
                   :source-paths ["dev"]}})
