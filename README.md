# aws-lambda

An opinionated Leiningen plugin to automate AWS Lambda deployments, and a template to create projects that use it.

## Getting Started

### AWS Credentials

Deploying to Lambda requires that you provide your AWS credentials (your access key and your secret key). You can do this using any of the methods supported by Amazon’s [Default Credential Provider Chain](http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html#credentials-default). You might, for example, set the `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` environment variables.

### Create a new project

Create a new project:

```ShellSession
$ lein new lambda-api «name»
```

### Deploy to production:

```ShellSession
$ lein lambda deploy production
«Deployment messages»
URL: https://7zxad5v8l2.execute-api.eu-west-1.amazonaws.com/production/
```

The URL printed at the end of the deployment process is the API Gateway endpoint at which it’s been published (yours will be different—API Gateway will generate one for you). You can confirm that it’s working with `curl`:

```ShellSession
$ curl https://7zxad5v8l2.execute-api.eu-west-1.amazonaws.com/production/hello
{"message":"Hello World"}
```

### Deploy to staging:

```ShellSession
$ lein lambda deploy staging
«Deployment messages»
URL: https://7zxad5v8l2.execute-api.eu-west-1.amazonaws.com/staging/
```

### Examine current deployments:

```ShellSession
$ lein lambda versions production
Published versions of function «name»:
  1 -> production
  2 -> staging
```
### Promote from staging to production:

```ShellSession
$ lein lambda promote production staging
Promoting production to version 2
$ lein lambda versions production
Published versions of function greeter-api:
  1
  2 -> production, staging
```

## Configuration

This plugin allows you to deploy an AWS Lambda function written in Clojure. It allows you to have multiple stages (typically "production" and "staging"). Each time you deploy, a new version of the Lambda function is created. Stages can be updated (promoted) to point to a specific version as needed.

Optionally, you can create an API Gateway API that forwards HTTP requests to your Lambda function. Corresponding API Gateway stages will be created for each of your Lambda function's stages.

Optionally, you can setup a "warmup" event that will keep your Lambda function warm by calling it periodically.

Here is a minimal configuration with two stages "production" and "staging":

```Clojure
:lambda {:function {:name "my-api"
                    :handler "my-api.lambda.LambdaFn"}
         :api-gateway {:name "my-api"}
         :stages {"production" {:warmup {:enable true}}
                  "staging"    {}}})
```

Here's a complete list of all configuration options:

```Clojure
:lambda {:credentials {:access-key "..." ; Your AWS credentials etc.
                       :secret-key "..." ;   Typically you won't specify them here, but instead
                       :endpoint "..."   ;   use any of the mechanisms supported by the AWS
                       :region "..."     ;   Default Credential Provider Chain
                       :profile "..."}

         :function {:name "..."          ; Name of your Lambda function (required)
                    :handler "..."       ; Name of your handler (required)
                    :description "..."   ; Optional
                    :memory-size 512     ; Optional, 512MB by default
                    :timeout 60          ; Optional, 60 seconds by default
                    :role "..."}         ; Optional, will be created if not specified
         :s3 {:bucket "..."}             ; Optional, will be created if not specified
         :api-gateway {:name "..."}      ; Optional
         :warmup {:enable false}         ; Optional, false by default
         :stages {"stage1" {...}         ; Stage definitions.
                  "stage2" {...}}})      ;   All above settings can be overridden per-stage
```

## Alteratives

This plugin was inspired by, but takes a slightly different approach from [clj-lambda-utils](https://github.com/mhjort/clj-lambda-utils)

## License

Copyright © 2017 Paul Butcher

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
