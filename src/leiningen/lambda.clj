(ns leiningen.lambda
  (:use [amazonica.aws.lambda]))

(defn lambda
  "TODO: Write documentation"
  [project & args]
  (println (list-functions)))
