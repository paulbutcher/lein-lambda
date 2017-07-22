(ns lein-lambda.config
  (:require [leiningen.core.main :refer [abort]]
            [schema.core :as s]
            [clojure.pprint :refer [pprint]]
            [amazonica.core :as amazonica]))

(def BaseConfig
  {(s/optional-key :credentials) {(s/optional-key :access-key) s/Str
                                  (s/optional-key :secret-key) s/Str
                                  (s/optional-key :endpoint) s/Str
                                  (s/optional-key :region) s/Str}
   (s/optional-key :function) {(s/optional-key :name) s/Str
                               (s/optional-key :handler) s/Str
                               (s/optional-key :description) s/Str
                               (s/optional-key :memory-size) s/Int
                               (s/optional-key :timeout) s/Int
                               (s/optional-key :role) s/Str}
   (s/optional-key :s3) {:bucket s/Str}
   (s/optional-key :api-gateway) {:name s/Str}
   (s/optional-key :warmup) {:enable s/Bool}})

(def Config (merge BaseConfig
                   {:stages {s/Str BaseConfig}}))

(defn- get-config* [config stage]
  (s/validate Config config)
  (let [stage-config (get-in config [:stages stage])
        merge-config #(merge (config %) (stage-config %))]
    (when-not stage-config
      (abort (str "Unknown stage: " stage)))
    {:credentials (merge-config :credentials)
     :function    (merge-config :function)
     :s3          (merge-config :s3)
     :api-gateway (merge-config :api-gateway)
     :warmup      (merge-config :warmup)}))

(defn get-config [config stage]
  (let [c (get-config* config stage)
        {{:keys [access-key secret-key endpoint region]} :credentials} c]
    (amazonica/defcredential (cond-> {}
                              access-key (assoc :access-key access-key)
                              secret-key (assoc :secret-key secret-key)
                              endpoint (assoc :endpoint endpoint)
                              region (assoc :endpoint region)))
    c))
