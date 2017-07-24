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
             ["src/{{sanitized}}/foo.clj" (render "foo.clj" data)])))
