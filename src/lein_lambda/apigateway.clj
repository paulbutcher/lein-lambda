(ns lein-lambda.apigateway
  (:use [amazonica.aws.apigateway]))

(defn- find-api [name]
  (let [apis ((get-rest-apis :limit 500) :items)]
    (first (filter #(get % :name) apis))))

(defn- maybe-create-api [name]
  (:id (or
         (find-api name)
         (create-rest-api :name name))))

(def root-path "/")
(def proxy-path-part "{proxy+}")
(def proxy-path (str root-path proxy-path-part))

(defn- find-path [path resources]
  (some->> resources
    (filter #(= path (% :path)))
    (first)
    (:id)))

(defn- get-resource-ids [api-id]
  (let [resources ((get-resources :restapi-id api-id) :items)]
    [(find-path root-path resources)
     (find-path proxy-path resources)]))

(defn- maybe-create-proxy-resource [api-id]
  (let [[root-id proxy-id] (get-resource-ids api-id)]
    (or proxy-id
        (:id (create-resource :restapi-id api-id
                              :parent-id root-id
                              :path-part proxy-path-part)))))

(defn- maybe-create-method [api-id proxy-id]
  (put-method :restapi-id api-id
              :resource-id proxy-id
              :http-method "ANY"
              :authorization-type "NONE"
              :request-parameters {"method.request.path.proxy" true}))

(defn deploy [{{:keys [name]} :api-gateway}]
  (when name
    (let [api-id (maybe-create-api name)
          proxy-id (maybe-create-proxy-resource api-id)
          method-id (maybe-create-method api-id proxy-id)])))    
