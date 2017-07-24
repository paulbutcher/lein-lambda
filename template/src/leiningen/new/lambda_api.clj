(ns leiningen.new.lambda-api
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files]]
            [leiningen.core.main :as main]))

(def render (renderer "lambda-api"))

(defn lambda-api
  "FIXME: write documentation"
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (main/info "Generating fresh 'lein new' lambda-api project.")
    (->files data
             ["src/{{sanitized}}/handler.clj" (render "handler.clj" data)]
             ["src/{{sanitized}}/lambda.clj" (render "lambda.clj" data)]
             ["test/{{sanitized}}/handler_test.clj" (render "handler_test.clj" data)]
             ["project.clj" (render "project.clj" data)]
             [".gitignore" (render "gitignore" data)])))
