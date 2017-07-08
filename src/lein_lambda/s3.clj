(ns lein-lambda.s3
  (:require [lein-lambda.identitymanagement :as identitymanagement])
  (:use [amazonica.aws.s3]))

(defn bucket-name [{{:keys [bucket]} :s3 {:keys [function-name]} :function}]
  (or bucket
    (str function-name "-" (identitymanagement/account-id))))

(defn bucket-key []
  "jar-file")

(defn- bucket-exists? [bucket-name]
  (try
    (head-bucket :bucket-name bucket-name)
    (catch Exception _ false)))

; Only create a bucket if no explicit bucket name is given
(defn- create-bucket-if-necessary [{{:keys [bucket]} :s3 :as config}]
  (let [bucket-name (bucket-name config)]
    (when-not (or bucket (bucket-exists? bucket-name))
      (println "Creating bucket" bucket-name)
      (create-bucket bucket-name))))

(defn upload [config jar-file]
  (let [bucket-name (bucket-name config)]
    (create-bucket-if-necessary config)
    (identitymanagement/account-id)
    (println "Uploading" jar-file "to bucket" bucket-name)
    (put-object :bucket-name bucket-name
                :key (bucket-key)
                :file jar-file)
    (println "Upload complete.")))
