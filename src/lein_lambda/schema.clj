(ns lein-lambda.schema
  (:require [schema.core :as s]))

(def Config
  {:function-name s/Str
   :handler s/Str
   (s/optional-key :description) s/Str
   (s/optional-key :memory-size) s/Int
   (s/optional-key :timeout) s/Int
   (s/optional-key :s3) {:bucket s/Str}
   (s/optional-key :role) {(s/optional-key :name) s/Str
                           (s/optional-key :arn) s/Str}})

(defn validate-config [config]
  (s/validate Config config))
