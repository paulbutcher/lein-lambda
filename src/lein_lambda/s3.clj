(ns lein-lambda.s3
  (:require [lein-lambda.identitymanagement :as identitymanagement]
            [amazonica.aws.s3 :as amazon]))

(defn bucket-name [{{:keys [bucket]} :s3 {:keys [function-name]} :function} stage]
  (or bucket
    (str function-name "-" stage "-" (identitymanagement/account-id))))

(defn bucket-key []
  "jar-file")

(defn- bucket-exists? [bucket-name]
  (try
    (amazon/head-bucket :bucket-name bucket-name)
    (catch Exception _ false)))

; Only create a bucket if no explicit bucket name is given
(defn- create-bucket-if-necessary [{{:keys [bucket]} :s3 :as config} stage]
  (let [bucket-name (bucket-name config stage)]
    (when-not (or bucket (bucket-exists? bucket-name))
      (println "Creating bucket" bucket-name)
      (amazon/create-bucket bucket-name))
    bucket-name))

(defn upload [config jar-file stage]
  (let [bucket-name (create-bucket-if-necessary config stage)]
    (println "Uploading" jar-file "to bucket" bucket-name)
    (amazon/put-object :bucket-name bucket-name
                       :key (bucket-key)
                       :file jar-file)
    (println "Upload complete.")))
