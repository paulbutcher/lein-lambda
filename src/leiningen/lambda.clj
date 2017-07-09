(ns leiningen.lambda
  (:require [leiningen.uberjar :refer [uberjar]]
            [lein-lambda.schema :refer [validate-config]]
            [lein-lambda.s3 :as s3]
            [lein-lambda.lambda :as lambda]
            [lein-lambda.apigateway :as apigateway]
            [lein-lambda.cloudwatchevents :as cloudwatchevents]))

(defn- deploy [project config]
  (let [jar-file (uberjar project)]
    (s3/upload config jar-file))
  (let [function-arn (lambda/deploy config)]
    (apigateway/deploy config function-arn)
    (cloudwatchevents/deploy config function-arn)))

(defn lambda
  "TODO: Write documentation"
  [project action & args]
  (let [config (project :lambda)]
    (validate-config config)
    (case action
      "deploy" (deploy project config))))
