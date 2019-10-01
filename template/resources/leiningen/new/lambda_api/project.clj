(defproject {{name}} "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [uswitch/lambada "0.1.2"]
                 [cheshire "5.7.1"]
                 [ring-apigw-lambda-proxy "0.3.0"]]
  :plugins [[lein-ring "0.12.5"]
            [lein-cljfmt "0.6.4"]
            [lein-lambda "0.2.0"]]
  :ring {:handler {{name}}.handler/app}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.1"]]}
             :uberjar {:aot :all}}
  :lambda {:function {:name "{{name}}"
                      :handler "{{name}}.lambda.LambdaFn"}
           :api-gateway {:name "{{name}}"}
           :stages {"production" {:warmup {:enable true}}
                    "staging" {}}})
