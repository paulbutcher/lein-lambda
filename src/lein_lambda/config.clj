(ns lein-lambda.config
  (:require [schema.core :as s]
            [clojure.pprint :refer [pprint]]))

(def BaseConfig
  {(s/optional-key :function) {(s/optional-key :function-name) s/Str
                               (s/optional-key :handler) s/Str
                               (s/optional-key :description) s/Str
                               (s/optional-key :memory-size) s/Int
                               (s/optional-key :timeout) s/Int
                               (s/optional-key :role) s/Str}
   (s/optional-key :s3) {:bucket s/Str}
   (s/optional-key :api-gateway) {:name s/Str}
   (s/optional-key :warmup) {:enable s/Bool}})

(def Config (merge BaseConfig
                   {(s/optional-key :stages) {s/Str BaseConfig}}))

(defn get-config [config stage]
  (s/validate Config config)
  (let [stage-config (get-in config [:stages stage])
        merge-config #(merge (config %) (stage-config %))]
    {:function    (merge-config :function)
     :s3          (merge-config :s3)
     :api-gateway (merge-config :api-gateway)
     :warmup      (merge-config :warmup)}))
