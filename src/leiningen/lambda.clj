(ns leiningen.lambda
  (:require [leiningen.uberjar :refer [uberjar]]
            [leiningen.core.main :refer [warn]]
            [clojure.pprint :refer [pprint]]
            [lein-lambda.config :refer [get-config]]
            [lein-lambda.s3 :as s3]
            [lein-lambda.lambda :as lambda]
            [lein-lambda.apigateway :as apigateway]
            [lein-lambda.cloudwatchevents :as cloudwatchevents]))

(defn- deploy [project config stage]
  (let [jar-file (uberjar project)]
    (s3/upload config jar-file))
  (let [function-arn (lambda/deploy config stage)]
    (apigateway/deploy config function-arn stage)
    (cloudwatchevents/deploy config function-arn stage)))

(defn- versions [project config stage]
  (lambda/versions config stage))

(defn lambda
  "TODO: Write documentation"
  [project action stage & args]
  (let [config (get-config (project :lambda) stage)]
    (case action
      "deploy" (deploy project config stage)
      "versions" (versions project config stage)
     (warn (str "Unknown task: " action)))))
