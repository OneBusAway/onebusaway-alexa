# OneBusAway skill for Alexa

Are you ready to ask about your bus with just your voice?

This project is an implementation of the [OneBusAway](http://onebusaway.org/) _open-source platform for real-time transit info_ for Amazon's [Alexa Voice Service](https://developer.amazon.com/public/solutions/alexa/alexa-voice-service).

## Install and Use

This project isn't released publicly yet.  We plan to make the first release around January 2016.

Initial spoken setup: TBD

Supported voice commands: TBD

## Develop

1. Install the [Java Platform SE Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Maven](https://maven.apache.org/).
1. Clone this repository.
1. Build this project on the command line with `mvn package`.  Look for "BUILD SUCCESS". Resulting JAR is `target/onebusaway-alexa-1.0-jar-with-dependencies.jar`
1. Upload to Amazon Lambda with:

        mvn lambduh:deploy-lambda \
            -DaccessKey={your_key} \
            -DsecretKey={your_key} \
            -Ds3Bucket={your_bucket} \
            -Dregion=us-east-1 \
            -DlambdaRoleArn=arn:aws:iam::{your_arn}:role/lambda_basic_execution

  ...where `{your_key}` is your AWS keys, `{your_bucket}` is your S3 bucket, and `{your_arn}` is your AWS Lambda ARN.
  
See the [lambduh plugin homepage](https://github.com/SeanRoy/lambduh-maven-plugin) for more information on deploying.

## Authors
* [Sean Barbeau](https://github.com/barbeau)

## Contributors
* [Philip White](https://github.com/philipmw)
