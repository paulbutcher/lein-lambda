(ns lein-lambda.identitymanagement
  (:require [clojure.string :as string])
  (:use [amazonica.aws.identitymanagement]))

; Sadly, this seems to be the only way to get the account ID :-(
; https://stackoverflow.com/questions/10197784/how-can-i-deduce-the-aws-account-id-from-available-basicawscredentials
(defn account-id []
  (-> (get-user)
    (get-in [:user :arn])
    (string/split #":")
    (nth 4)))

(defn- get-role-arn [name]
  (get-in (get-role :role-name name) [:role :arn]))

(defn- maybe-get-role-arn [name]
  (try
    (get-role-arn name)
    (catch Exception _ false)))

(def trust-policy "{\"Version\":\"2012-10-17\",\"Statement\":{\"Effect\":\"Allow\",\"Principal\":{\"Service\":\"lambda.amazonaws.com\"},\"Action\":\"sts:AssumeRole\"}}")

(defn- deploy-role [role-name]
  (println "Creating policy" role-name)
  (let [role (create-role :role-name role-name
                          :assume-role-policy-document trust-policy)]
    (attach-role-policy :policy-arn "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
                        :role-name role-name)
    (get-in role [:role :arn])))

(defn- maybe-deploy-role [{:keys [function-name]}]
  (let [role-name (str function-name "-lambda-" (account-id))]
    (if-let [arn (maybe-get-role-arn role-name)]
      arn
      (deploy-role role-name))))

(defn role-arn [{{:keys [arn name]} :role :as options}]
  (cond
    arn arn
    name (get-role-arn name)
    :else (maybe-deploy-role options)))
