(ns lein-lambda.s3
  (:use [amazonica.aws.s3]))

(defn bucket-name [{{:keys [bucket]} :s3}]
  bucket)

(defn bucket-key []
  "jar-file")

(defn upload [config jar-file]
  (let [bucket-name (bucket-name config)]
    ; TODO - create bucket if necessary
    (println "Uploading" jar-file "to bucket" bucket-name)
    (put-object :bucket-name bucket-name
                :key (bucket-key)
                :file jar-file)
    (println "Upload complete.")))
