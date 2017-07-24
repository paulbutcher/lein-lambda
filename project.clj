(defproject lein-lambda "0.2.0-SNAPSHOT"
  :description "A Leiningen plugin to automate AWS Lambda deployments"
  :url "https://github.com/paulbutcher/lein-lambda"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.amazonaws/aws-java-sdk-bundle "1.11.160"]
                 [amazonica "0.3.106" :exclusions [com.amazonaws/aws-java-sdk
                                                   com.amazonaws/amazon-kinesis-client
                                                   com.amazonaws/dynamodb-streams-kinesis-adapter]]
                 [prismatic/schema "1.1.6"]
                 [robert/bruce "0.8.0"]]
  :eval-in-leiningen true)
