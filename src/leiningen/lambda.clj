(ns leiningen.lambda
  (:require [leiningen.uberjar :refer [uberjar]]
            [lein-lambda.schema :refer [validate-config]]
            [lein-lambda.s3 :as s3]
            [lein-lambda.lambda :as lambda]))

(defn- deploy [project config]
  (let [jar-file (uberjar project)]
    (s3/upload config jar-file)
    (lambda/update-function config)))

(defn lambda
  "TODO: Write documentation"
  [project action & args]
  (let [config (project :lambda)]
    (validate-config config)
    (case action
      "deploy" (deploy project config))))
