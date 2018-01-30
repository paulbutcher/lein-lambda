(ns lein-lambda.apigateway
  (:require [amazonica.aws.apigateway :as amazon]
            [lein-lambda.lambda :as lambda]))

(defn- target-arn [region account-id]
  (str "arn:aws:apigateway:" 
       region
       ":lambda:path/2015-03-31/functions/arn:aws:lambda:"
       region
       ":"
       account-id
       ":function:${stageVariables.functionname}:${stageVariables.stage}/invocations"))

(defn- source-arn [api-id region account-id]
  (str "arn:aws:execute-api:"
       region
       ":"
       account-id
       ":"
       api-id
       "/*/*/*"))

(defn- find-api [name]
  (:id (let [apis ((amazon/get-rest-apis :limit 500) :items)]
         (first (filter #(= (get % :name) name) apis)))))

(defn- create-api [name]
  (println "Creating API:" name)
  (:id (amazon/create-rest-api :name name)))

(defn- maybe-create-api [name]
  (or
    (find-api name)
    (create-api name)))

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

(defn- create-proxy-resource [api-id root-id]
  (println "Creating proxy resource")
  (:id (amazon/create-resource :restapi-id api-id
                               :parent-id root-id
                               :path-part proxy-path-part)))

(defn- maybe-create-proxy-resource [api-id root-id proxy-id]
  (or proxy-id
    (create-proxy-resource api-id root-id)))

(def http-method "ANY")

(defn- find-method [api-id proxy-id]
  (try
    (amazon/get-method :http-method http-method
                       :resource-id proxy-id
                       :restapi-id api-id)
    (catch Exception _ false)))

(defn- create-method [api-id proxy-id]
  (println "Creating method")
  (amazon/put-method :restapi-id api-id
                     :resource-id proxy-id
                     :http-method http-method
                     :authorization-type "NONE"
                     :request-parameters {"method.request.path.proxy" true}))

(defn- maybe-create-method [api-id proxy-id]
  (or
    (find-method api-id proxy-id)
    (create-method api-id proxy-id)))

(defn- find-integration [api-id proxy-id]
  (try
    (amazon/get-integration :http-method http-method
                            :resource-id proxy-id
                            :restapi-id api-id)
    (catch Exception _ false)))

(defn- create-integration [api-id proxy-id region account-id]
  (println "Creating integration")
  (amazon/put-integration :restapi-id api-id
                          :resource-id proxy-id
                          :http-method http-method
                          :integration-http-method "POST"
                          :type "AWS_PROXY"
                          :passthrough-behavior "WHEN_NO_MATCH"
                          :uri (target-arn region account-id)))

(defn- maybe-create-integration [api-id proxy-id region account-id]
  (or
    (find-integration api-id proxy-id)
    (create-integration api-id proxy-id region account-id)))  

(defn find-stage [api-id stage]
  (try
    (amazon/get-stage :restapi-id api-id
                      :stage-name stage)
    (catch Exception _ false)))

(defn- create-deployment [api-id region account-id function-name stage]
  (println "Creating deployment")
  (amazon/create-deployment :restapi-id api-id
                            :stage-name stage
                            :variables {"functionname" function-name
                                        "stage" stage})
  (lambda/allow-api-gateway function-name
                            (source-arn api-id region account-id)
                            stage))

(defn- maybe-create-deployment [api-id region account-id function-name stage]
  (or
    (find-stage api-id stage)
    (create-deployment api-id region account-id function-name stage)))

(defn deploy [{{:keys [name]} :api-gateway} function-arn stage]
  (when name
    (let [[region account-id function-name] (lambda/get-arn-components function-arn)
          api-id (maybe-create-api name)
          [root-id proxy-id] (get-resource-ids api-id)]
      (let [proxy-id (maybe-create-proxy-resource api-id root-id proxy-id)
            method-id (maybe-create-method api-id proxy-id)]
        (maybe-create-integration api-id proxy-id region account-id)
        (maybe-create-deployment api-id region account-id function-name stage)
        (println (str "URL: https://" api-id ".execute-api." region ".amazonaws.com/" stage "/"))))))
