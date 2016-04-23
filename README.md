# OneBusAway skill for Alexa

[![Build Status](https://travis-ci.org/OneBusAway/onebusaway-alexa.svg?branch=master)](https://travis-ci.org/OneBusAway/onebusaway-alexa)
[![Coverage Status](https://coveralls.io/repos/github/OneBusAway/onebusaway-alexa/badge.svg?branch=master)](https://coveralls.io/github/OneBusAway/onebusaway-alexa?branch=master)
[![Join the OneBusAway chat](https://onebusaway.herokuapp.com/badge.svg)](https://onebusaway.herokuapp.com/)

Are you ready to ask your [Amazon Echo](http://www.amazon.com/echo), *"Alexa, where's my bus?"*

This project is an implementation of the [OneBusAway](http://onebusaway.org/) open-source platform for real-time transit info for Amazon's [Alexa Voice Service](https://developer.amazon.com/public/solutions/alexa/alexa-voice-service).

## Install and Use ##

This project isn't released publicly yet.  We plan to make the first release in early 2016.

Never miss the bus again!

Just say "Alexa, open OneBusAway" to get started.  You'll set your city and favorite stop, using the stop number.  Then, just say "Alexa, open OneBusAway" to get arrival times for your favorite stop.  You can also change your city or stop at any point, or ask OneBusAway to repeat the last information.

You can find your stop number on the placard in the bus zone, in your OneBusAway mobile app, or on your agency website.  If you have trouble finding your stop number, see our guide for ["Finding your stop number"](https://github.com/OneBusAway/onebusaway-alexa/wiki/Finding-your-stop-number).

Refer to [`interaction model/utterances.txt`](interaction%20model/utterances.txt) for the full list of spoken phrases we support.

Our [user interface flow diagram](USER_INTERFACE_FLOW.md) also defined how you can interact with the skill.

## Develop ##

The application backing the skill was designed to run in AWS.

### Prepare your AWS environment

1. Log in to your [AWS Console](http://console.aws.amazon.com) and switch to the "N. Virginia" region.
1. Apply the CloudFormation template in `aws/cloudformation/onebusaway.template` in your AWS account.
   Name your stack "onebusaway-alexa" for consistency with existing documentation.
1. Acknowledge "that this template might cause AWS CloudFormation to create IAM resources."
1. Switch to the Output tab of your stack and note the AWS key ID and secret key.
   You will use these in the next section.

### Personalize the app

1. Create `src/main/resources/onebusaway.properties` with the following content:

```
skill-app-id-development=amzn1.echo-sdk-ams.app... //You'll get this when setting up your Skill. See below.
aws.key-id=...
aws.secret-key=...
googlemaps.api-key=...
onebusaway.api-key=...
```

And fill it in with your own values.

### Build the skill

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
  Note that the AWS credentials used here are distinct from those you just added
  to `onebusaway.properties`!  Those are for *running* the application, whereas these
  are for whichever IAM user you allow to deploy Lambda functions.
1. From the AWS Console, open your Lambda function and note its ARN.
   You will use it in the next section.
1. Add "Alexa Skills Kit" as a new _Event Source_.

See the [lambduh plugin homepage](https://github.com/SeanRoy/lambduh-maven-plugin) for more information on deploying.

### Contributing

Want to make OneBusAway Alexa better?  We welcome collaboration!  See our [Contributing Guide](.github/CONTRIBUTING.md) for more details.

### Deploy to your Alexa device for the first time

This will be a bit circuitous, because you want your skill to run only if triggered by
Alexa, rather than some random Internet visitor or script kiddie.  But to do this, we
must create the skill first, to get the unique skill ID.  But we cannot get this until
deploying the skill, which requires your skill to be already running in Lambda.
That's why we deployed the skill even though it won't work yet.

1. Go to the [Amazon Developer Console](https://developer.amazon.com/edw/home.html)
1. Add a new skill.  Set _Invocation Name_ to "one bus away".  Plug in your Lambda function's ARN.
1. Into "Intent Schema" text box, paste the contents of file `interaction model/schema.json`.
1. Under "Custom Slot Types," click on "Add Slot Type".  Under "Enter type", add `TRANSIT_MODES`.  Under "Enter values", paste the contents of the file `interaction model/customSlotTypes/TRANSIT_MODES`. 
1. Under "Custom Slot Types," click on "Add Slot Type".  Under "Enter type", add `AMAZON.US_CITY`.  Under "Enter values", paste the contents of the file `interaction model/customSlotTypes/AMAZON.US_CITY`.
1. Into "Sample Utterances" text box, paste the contents of file `interaction model/utterances.txt`.
   Go Next.  That creates the skill.
1. Switch back to "Skill Information" section, the first section of wizard.
1. Note the _Application Id_.  Copy that into `src/main/resources/onebusaway.properties`
   under the `skill-app-id-development=amzn1...` entry.
1. Now recompile and re-deploy to Lambda!

CAUTION: Every time you re-deploy to Lambda using the `lambduh-maven-plugin`, you must
manually re-add "Alexa Skills Kit" as the function's _Event Source_.  You do not need to
do this if you deploy your code through the Lambda UI in AWS Console.

### Authors
* [Sean Barbeau](https://github.com/barbeau)
* [Philip White](https://github.com/philipmw)

### License

[Apache v2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Privacy Policy

See our [Privacy Policy](http://onebusaway.org/privacy/) to better understand what information the OneBusAway Alexa skill uses when you request transit arrival information.
