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
