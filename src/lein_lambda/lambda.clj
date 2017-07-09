(ns lein-lambda.lambda
  (:require [lein-lambda.s3 :as s3]
            [lein-lambda.identitymanagement :as identitymanagement]
            [robert.bruce :refer [try-try-again]]
            [amazonica.aws.lambda :as amazon]))

(defn- function-config [{{:keys [function-name handler memory-size timeout role description]
                          :or {memory-size 512 timeout 60 description ""}} :function
                         :as config}]
  {:function-name function-name
   :handler handler
   :memory-size memory-size
   :timeout timeout
   :role (identitymanagement/role-arn role config)
   :runtime "java8"
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
(defn- deploy-create [function-config]
  (println "Creating lambda function" (function-config :function-name))
  (try-try-again
    {:decay :exponential :sleep 1000 :tries 5}
    amazon/create-function function-config))

(defn- deploy-update [function-config]
  (println "Updating lambda function" (function-config :function-name))
  (amazon/update-function-configuration function-config))

(defn deploy [config]
  (let [function-config (function-config config)]
    (if (function-exists? function-config)
      (deploy-update function-config)
      (deploy-create function-config))))
