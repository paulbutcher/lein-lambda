(ns {{name}}.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.util.response :as response :refer [response]]
            [ring.middleware.json :refer [wrap-json-response]]))

(defroutes app-routes
  (GET "/hello" []
    (response {:message "Hello World"}))

  (route/not-found
    (response/not-found {:message "Not Found"})))

(def app
  (-> app-routes
    (wrap-json-response)
    (wrap-defaults api-defaults)))
