(ns lein-lambda.identitymanagement
  (:require [clojure.string :as string]
            [amazonica.aws.identitymanagement :as amazon]
            [amazonica.aws.securitytoken :as sts]))

(defn account-id []
  (get-in (sts/get-caller-identity {}) [:account]))

(defn- get-role-arn [name]
  (get-in (amazon/get-role :role-name name) [:role :arn]))

(defn- maybe-get-role-arn [name]
  (try
    (get-role-arn name)
    (catch Exception _ false)))

(def trust-policy "{\"Version\":\"2012-10-17\",\"Statement\":{\"Effect\":\"Allow\",\"Principal\":{\"Service\":\"lambda.amazonaws.com\"},\"Action\":\"sts:AssumeRole\"}}")

(defn- deploy-role [role-name]
  (println "Creating policy" role-name)
  (let [role (amazon/create-role :role-name role-name
                                 :assume-role-policy-document trust-policy)]
    (amazon/attach-role-policy :policy-arn "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
                               :role-name role-name)
    (get-in role [:role :arn])))

(defn- maybe-deploy-role [function-name]
  (let [role-name (str function-name "-lambda")]
    (or
      (maybe-get-role-arn role-name)
      (deploy-role role-name))))

(defn role-arn [{{:keys [role name]} :function}]
  (if role
    (get-role-arn role)
    (maybe-deploy-role name)))
