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

The URL printed at the end of the deployment process is the API Gateway endpoint at which it’s been published (yours will be different—API Gateway will generate one for you). We can confirm that it’s working with `curl`:

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

## License

Copyright © 2017 Paul Butcher

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
