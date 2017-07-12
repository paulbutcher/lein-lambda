(ns lein-lambda.cloudwatchevents
  (:require [amazonica.aws.cloudwatchevents :as amazon]
            [lein-lambda.lambda :as lambda]))

(def rule-name "lambda-warmup")

(defn- find-rule []
  (try
    (:arn (amazon/describe-rule :name rule-name))
    (catch Exception _ false)))

(defn- create-rule []
  (println "Creating rule:" rule-name)
  (:rule-arn (amazon/put-rule :name rule-name
                              :schedule-expression "rate(5 minutes)"
                              :description "Keep lambda function warm")))

(defn- maybe-create-rule []
  (or
    (find-rule)
    (create-rule)))

(defn- find-target [function-arn]
  (->> (amazon/list-targets-by-rule :rule rule-name)
    (:targets)
    (filter #(= function-arn (:arn %)))
    (first)))

(defn- create-target [rule-arn function-arn]
  (println "Adding target to rule:" rule-name)
  (amazon/put-targets :targets [{:arn function-arn :id "warmup"}]
                      :rule rule-name)
  (let [[region account-id function-name] (lambda/get-arn-components function-arn)]
    (lambda/allow-wakeup function-name rule-arn)))

(defn- maybe-create-target [rule-arn function-arn]
  (or
    (find-target function-arn)
    (create-target rule-arn function-arn)))

(defn deploy [{{:keys [enable] :or {enable true}} :warmup} function-arn]
  (when enable
    (let [rule-arn (maybe-create-rule)]
      (maybe-create-target rule-arn function-arn))))
