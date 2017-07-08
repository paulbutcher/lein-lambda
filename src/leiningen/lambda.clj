(ns leiningen.lambda
  (:require [lein-lambda.schema :refer [validate-config]])
  (:use [amazonica.aws.lambda]))

(defn lambda
  "TODO: Write documentation"
  [project & args]
  (let [config (project :lambda)]
    (validate-config config)
    (println (list-functions))))
