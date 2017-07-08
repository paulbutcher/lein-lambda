(ns lein-lambda.s3
  (:use [amazonica.aws.s3]))

(defn upload [jar-file {{:keys [bucket]} :s3}]
  ; TODO - create bucket if necessary
  (println "Uploading" jar-file "to bucket" bucket)
  (put-object :bucket-name bucket
              :key "jar-file"
              :file jar-file)
  (println "Upload complete."))
