(ns lein-lambda.apigateway
  (:require [amazonica.aws.apigateway :as amazon]
            [clojure.string :as string]
            [lein-lambda.lambda :as lambda]))

(defn- find-api [name]
  (let [apis ((amazon/get-rest-apis :limit 500) :items)]
    (first (filter #(get % :name) apis))))

(defn- maybe-create-api [name]
  (:id (or
         (find-api name)
         (amazon/create-rest-api :name name))))

(def root-path "/")
(def proxy-path-part "{proxy+}")
(def proxy-path (str root-path proxy-path-part))

(defn- find-path [path resources]
  (some->> resources
    (filter #(= path (% :path)))
    (first)
    (:id)))

(defn- get-resource-ids [api-id]
  (let [resources ((amazon/get-resources :restapi-id api-id) :items)]
    [(find-path root-path resources)
     (find-path proxy-path resources)]))

(defn- maybe-create-proxy-resource [api-id root-id proxy-id]
  (or proxy-id
    (:id (amazon/create-resource :restapi-id api-id
                                 :parent-id root-id
                                 :path-part proxy-path-part))))

(def http-method "ANY")

(defn- find-method [api-id proxy-id]
  (try
    (amazon/get-method :http-method http-method
                       :resource-id proxy-id
                       :restapi-id api-id)
    (catch Exception _ false)))

(defn- maybe-create-method [api-id proxy-id]
  (or
    (find-method api-id proxy-id)
    (amazon/put-method :restapi-id api-id
                       :resource-id proxy-id
                       :http-method http-method
                       :authorization-type "NONE"
                       :request-parameters {"method.request.path.proxy" true})))

(defn- get-arn-components [function-arn]
  (let [components (string/split function-arn #":")]
    [(nth components 3) (nth components 4) (nth components 6)]))

(defn- target-arn [function-arn region]
  (str "arn:aws:apigateway:" 
       region
       ":lambda:path/2015-03-31/functions/"
       function-arn
       "/invocations"))

(defn- source-arn [api-id region account-id]
  (str "arn:aws:execute-api:"
       region
       ":"
       account-id
       ":"
       api-id
       "/*/*/*"))

(defn- find-integration [api-id proxy-id]
  (try
    (amazon/get-integration :http-method http-method
                            :resource-id proxy-id
                            :restapi-id api-id)
    (catch Exception _ false)))

(defn- create-integration [api-id proxy-id function-arn]
  (let [[region account-id function-name] (get-arn-components function-arn)]
    (amazon/put-integration :restapi-id api-id
                            :resource-id proxy-id
                            :http-method http-method
                            :integration-http-method "POST"
                            :type "AWS_PROXY"
                            :passthrough-behavior "WHEN_NO_MATCH"
                            :uri (target-arn function-arn region))
    (lambda/allow-api-gateway function-name
                              (source-arn api-id region account-id))))

(defn- maybe-create-integration [api-id proxy-id function-arn]
  (or
    (find-integration api-id proxy-id)
    (create-integration api-id proxy-id function-arn)))  

(def stage-name "production")

(defn find-stage [api-id]
  (try
    (amazon/get-stage :restapi-id api-id
                      :stage-name stage-name)
    (catch Exception _ false)))

(defn- maybe-create-deployment [api-id]
  (or
    (find-stage api-id)
    (amazon/create-deployment :restapi-id api-id
                              :stage-name stage-name)))

(defn deploy [{{:keys [name]} :api-gateway} function-arn]
  (when name
    (let [api-id (maybe-create-api name)
          [root-id proxy-id] (get-resource-ids api-id)]
      (let [proxy-id (maybe-create-proxy-resource api-id root-id proxy-id)
            method-id (maybe-create-method api-id proxy-id)]
        (maybe-create-integration api-id proxy-id function-arn)
        (maybe-create-deployment api-id)))))
