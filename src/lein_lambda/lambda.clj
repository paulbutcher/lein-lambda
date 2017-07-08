(ns lein-lambda.lambda
  (:require [lein-lambda.s3 :as s3])
  (:use [amazonica.aws.lambda]))

(defn function-config [{:keys [function-name handler memory-size timeout role description] :as config}]
  {:function-name function-name
   :handler handler
   :memory-size memory-size
   :timeout timeout
   :role (role :arn)
   :runtime "java8"
   :description description
   :code {:s3-bucket (s3/bucket-name config)}
         :s3-key (s3/bucket-key)})

(defn function-exists? [{:keys [function-name]}]
  (try
    (get-function :function-name function-name)
    (catch Exception _ false)))

(defn deploy [config]
  (let [function-config (function-config config)]
    (if (function-exists? function-config)
      (update-function-configuration function-config)
      (create-function function-config))))
