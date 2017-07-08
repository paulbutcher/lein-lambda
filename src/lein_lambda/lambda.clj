(ns lein-lambda.lambda
  (:require [lein-lambda.s3 :as s3])
  (:use [amazonica.aws.lambda]))

(defn update-function [{:keys [function-name handler memory-size timeout role description] :as config}]
  (create-function :function-name function-name
                   :handler handler
                   :memory-size memory-size
                   :timeout timeout
                   :role (role :arn)
                   :runtime "java8"
                   :description description
                   :code {:s3-bucket (s3/bucket-name config)
                          :s3-key (s3/bucket-key)}))
