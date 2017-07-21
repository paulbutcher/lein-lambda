(ns lein-lambda.lambda
  (:require [lein-lambda.s3 :as s3]
            [lein-lambda.identitymanagement :as identitymanagement]
            [robert.bruce :refer [try-try-again]]
            [amazonica.aws.lambda :as amazon]
            [clojure.string :as string]))

(defn get-arn-components [function-arn]
  (let [components (string/split function-arn #":")]
    [(nth components 3) (nth components 4) (nth components 6)]))

(defn- add-permission [function-name source-arn principal statement-id stage]
  (amazon/add-permission :function-name function-name
                         :qualifier stage
                         :action "lambda:InvokeFunction"
                         :principal principal
                         :source-arn source-arn
                         :statement-id statement-id))

(defn allow-api-gateway [function-name source-arn stage]
  (add-permission function-name 
                  source-arn
                  "apigateway.amazonaws.com"
                  "lein-lambda-apigateway"
                  stage))

(defn allow-wakeup [function-name source-arn stage]
  (add-permission function-name 
                  source-arn 
                  "events.amazonaws.com" 
                  "lein-lambda-warmup"
                  stage))

(defn- mk-function-config [{{:keys [name handler memory-size timeout description]
                             :or {memory-size 512 timeout 60 description ""}} :function
                            :as config}]
  {:function-name name
   :handler handler
   :memory-size memory-size
   :timeout timeout
   :role (identitymanagement/role-arn config)
   :runtime "java8"
   :publish true
   :description description
   :code {:s3-bucket (s3/bucket-name config)
          :s3-key (s3/bucket-key)}})

(defn- function-exists? [{:keys [function-name]}]
  (try
    (amazon/get-function :function-name function-name)
    (catch Exception _ false)))

; There seems to be a race condition in the Amazon API which can cause function creation
; to fail if the role used has only recently been created. So retry until this succeeds.
; See:
;   https://stackoverflow.com/questions/36419442/the-role-defined-for-the-function-cannot-be-assumed-by-lambda
;   https://stackoverflow.com/questions/37503075/invalidparametervalueexception-the-role-defined-for-the-function-cannot-be-assu
(defn- deploy-create [{:keys [function-name] :as function-config}]
  (println "Creating lambda function" function-name)
  (try-try-again
    {:decay :exponential :sleep 1000 :tries 5}
    amazon/create-function function-config))

(defn- deploy-update [{:keys [function-name code] :as function-config}]
  (println "Updating lambda function" function-name)
  (amazon/update-function-configuration function-config)
  ; Annoyingly, update-function-code takes its s3-bucket and s3-key arguments at the
  ; top level, meaning that we can't use function-config as-is :-(
  (amazon/update-function-code (merge function-config code)))

(defn- create-or-update [function-config]
  (if (function-exists? function-config)
    (deploy-update function-config)
    (deploy-create function-config)))

(defn- alias-exists? [function-name stage]
  (try
    (amazon/get-alias :function-name function-name
                      :name stage)
    (catch Exception _ false)))

(defn- create-or-update-alias [function-name stage version]
  (println "Setting alias" stage "to point to version" version)
  (let [alias-config {:function-name function-name
                      :name stage
                      :function-version version}]
    (if (alias-exists? function-name stage)
      (amazon/update-alias alias-config)
      (amazon/create-alias alias-config))))

(defn deploy [config stage]
  (let [{:keys [version function-name]} (create-or-update (mk-function-config config))]
    (println "Deployed as version" version)
    (:alias-arn (create-or-update-alias function-name stage version))))
