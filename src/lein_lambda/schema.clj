(ns lein-lambda.schema
  (:require [schema.core :as s]))

(def Config
  {:function-name s/Str
   :handler s/Str
   :memory-size s/Int
   :timeout s/Int
   :s3 {:bucket s/Str}
   :policy {:name s/Str}
   (s/optional-key :profile) s/Str
   (s/optional-key :region) s/Str})

(defn validate-config [config]
  (s/validate Config config))
