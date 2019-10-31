# OneBusAway skill for Alexa [![Build Status](https://travis-ci.org/OneBusAway/onebusaway-alexa.svg?branch=master)](https://travis-ci.org/OneBusAway/onebusaway-alexa) [![Coverage Status](https://coveralls.io/repos/github/OneBusAway/onebusaway-alexa/badge.svg?branch=master)](https://coveralls.io/github/OneBusAway/onebusaway-alexa?branch=master) [![Join the OneBusAway chat](https://onebusaway.herokuapp.com/badge.svg)](https://onebusaway.herokuapp.com/)

Are you ready to ask your [Amazon Echo](http://www.amazon.com/echo), *"Alexa, where's my bus?"*

This project is an implementation of the [OneBusAway](http://onebusaway.org/) open-source platform for real-time transit info for Amazon's [Alexa Voice Service](https://developer.amazon.com/public/solutions/alexa/alexa-voice-service).

## Install and Use

### Enabling the OneBusAway skill

To enable this skill on your Alexa device, you can say, "Enable OneBusAway skill." 

You can also search for and enable skills in the Alexa app: 

1. Open the Alexa app.
1. Open the left navigation panel, and then select Skills.
1. Use the search bar to enter "OneBusAway".
1. Tap on the "OneBusAway" skill, and select Enable Skill.

### Using the OneBusAway skill

Just say "Alexa, open OneBusAway" to get started. You'll set your city and favorite stop, using the stop number. Then, just say "Alexa, open OneBusAway" to get arrival times for your favorite stop. You can find your stop number on the placard in the bus zone, in your OneBusAway mobile app, or on your agency website. If you have trouble finding your stop number, see our guide for ["Finding your stop number"](https://github.com/OneBusAway/onebusaway-alexa/wiki/Finding-your-stop-number).  

Other features - say `Alexa, ask OneBusAway to...`:
* `...filter routes` to filter out arrivals for particular routes for your currently selected stop
* `...enable clock times` to announce times in a clock format like "at 10:25am" instead of "in 5 minutes"
* `...set my stop to X` to change your stop to the specified number
* `...set my city to X` to change your city to the specified city
* `...repeat` to repeat the last message
* `...enable experimental regions` to enable regions that may be unstable and without real-time info

Refer to [`interaction model/utterances.txt`](interaction%20model/utterances.txt) for the full list of spoken phrases we support.

Our [user interface flow diagram](USER_INTERFACE_FLOW.md) also defined how you can interact with the skill.

### Available cities

The OneBusAway skill is currently available in the following cities:

* Atlanta, GA
* Puget Sound, WA
* Rogue Valley Transportation District, Oregon
* San Diego Metropolitan Transit System, California
* Spokane, WA
* Tampa, FL
* Washington, D.C.
* York, Canada

See [OneBusAway Deployments](http://onebusaway.org/onebusaway-deployments/) for more information about available OneBusAway regions.

### Privacy Policy
See our [Privacy Policy](http://onebusaway.org/privacy/) to better understand what information the OneBusAway Alexa skill uses when you request transit arrival information.

## Contributing
Want to make OneBusAway Alexa better?  We welcome collaboration!  See our [Contributing Guide](.github/CONTRIBUTING.md) for more details.

## Develop
The application backing the skill was designed to run in AWS.

The set up process will be a bit circuitous, because, for security reasons, you want Lambda to run your code _only_ if triggered by
Alexa, rather than some random Internet visitor or script kiddie. To do this, we must create a skill first, and then supply its unique skill id to Lambda. But, to create a skill, a backing Lambda function should already be deployed. This is resolved by deploying a placeholder Lambda function first, and updating it later.

Therefore, the process is as follows:

1. Prepare AWS environment
1. Build the project
1. Deploy Lambda function. At this point this is only a placeholder needed to proceed with Alexa Skill set up.
1. Set up Alexa Skill backed by the Lambda function.
1. Note Alexa Skill id, supply it to the Lambda function, rebuild and redeploy.

### 1. Prepare AWS environment
1. Log into your [AWS Console](http://console.aws.amazon.com) and switch to the "N. Virginia" region (currently, the only region that supports Alexa development)
1. Сreate a new CloudFormation Stack from the template `aws/cloudformation/onebusaway.template`.
1. Name your stack "onebusaway-alexa" for consistency with existing documentation.
1. On Review page under Capabilities section acknowledge "that this template might cause AWS CloudFormation to create IAM resources."
1. Click "Create" and wait for AWS to complete execution.
1. Switch to the Output tab of your new stack and note the output parameters. These are access keys and resource ARNs which you will need in the next sections.

You can examine AWS resources created from this template on the Resources tab.

Note that two distinct sets of AWS access credentials are being generated: `{lambdaDeploymentAccessKey}` and `{lambdaDeploymentSecretKey}` are for *deploying* Lambda functions, whereas `{appExecutionAccessKey}` and `{appExecutionAccessSecret}` are for *running* the application.

You can also use CloudFormation from command line with [AWS CLI](http://aws.amazon.com/cli/).

### 2. Build the project
1. Install the [Java Platform SE Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Maven](https://maven.apache.org/).
1. Clone this repository.
1. Build this project on the command line with `mvn package`.  Look for "BUILD SUCCESS". Resulting JAR is `target/onebusaway-alexa-1.0-jar-with-dependencies.jar`

### 3. Deploy Lambda function
1. Upload to Amazon Lambda with:
```
        mvn lambda:deploy-lambda \
            -DaccessKey={lambdaDeploymentAccessKey} \
            -DsecretKey={lambdaDeploymentSecretKey} \
            -Ds3Bucket={lambdaDeploymentS3Bucket} \
            -Dregion=us-east-1 \
            -DlambdaRoleArn={lambdaExecutionRoleARN}
```
...where `{lambdaDeploymentAccessKey}`, `{lambdaDeploymentSecretKey}`, `{lambdaDeploymentS3Bucket}` and `{lambdaExecutionRoleARN}` are values generated by CloudFormation during AWS infrastructure set up.

1. From the [AWS Console > Lambda](https://console.aws.amazon.com/lambda), open the newly created Lambda function and add a _Trigger_ (Event Source) of type `Alexa Skills Kit`.
1. Note the ARN of the Lambda function at the top right corner of the screen. You will use it to set up a new Alexa Skill in the next section.

See the [lambda maven plugin homepage](https://github.com/SeanRoy/lambda-maven-plugin) for more information on deploying.

### 4. Set up Alexa Skill

1. Go to the [Amazon Developer Console > Alexa](http://developer.amazon.com/edw/home.html)
1. Add a new skill.  Set _Skill Type_ to `Custom Interaction Model`, set _Invocation Name_ to "one bus away". _Name_ can be anything since this is your development version.
1. Paste the contents of file `interaction model/newSchema.json` into "JSON Editor" under interaction model.
1. Save and build the model.
1. On Endpoint page set up your endpoint by plugging in your Lambda function's ARN.
   Go Next.  That creates the skill, however it is not functional yet.
1. At the top of the screen note application _ID_. You will use it to configure Lambda code.
#### 4.1 Personalize your skill
To set up your skill to support personalization, you must follow these steps.

1. Set up your skill to request personalization permissions.When you create or edit a custom skill, you can turn on permissions for Skill Personalization in the developer console.
    * In the developer console, create or open your skill. 
    * Select the Build tab, and select Permissions at the bottom left.
    * In the Permissions section, turn on the toggle for Skill Personalization.
    * You can also edit the permissions in the skill manifest for your skill directly to add the personalization scope alexa::person_id:read if you are using SMAPI or ASK CLI to build your skill.
1. Set up your skill service to use personalization in its responses, if Personalize skills is toggled on for a recognized user.
1. Make sure your skill service gracefully handles those cases where a user is not recognized, or a user refuses permission for personalization.

#### 4.2 Listen to skill event (OPTIONAL)
1. Currently OneBusAway supports [SkillEnabled](https://developer.amazon.com/docs/smapi/skill-events-in-alexa-skills.html#skill-enabled-event) and [SkillDisabled](https://developer.amazon.com/docs/smapi/skill-events-in-alexa-skills.html#skill-disabled-event) events.
1. The **ONLY WAY** to enable your test skill to listen to events is using Skill Management API (SMAPI), please follow the [Quick Start: Alexa Skills Kit Command Line Interface (ASK CLI)](https://developer.amazon.com/docs/smapi/quick-start-alexa-skills-kit-command-line-interface.html) to install ASK CLI.
1. After you setup CLI, please using `ask api get-skill -s {skillId} > skill.json` to get your skill schema in `skill.json` file, add the event subscription JSON blob below to skill.json, and run
`ask api update-skill -s {skillId} -f skill.json > skill.json` to update the schema, [reference: Add Events to Your Skill
](https://developer.amazon.com/docs/smapi/add-events-to-your-skill-with-smapi.html)

Event subscription JSON blob.
```json
"event": {
  "endpoint": {
    "uri": "{lambdaExecutionRoleARN}"
  },
  "regions": {
    "NA": {
      "endpoint": {
        "uri": "{lambdaExecutionRoleARN}"
      }
    }
  },
  "subscriptions": [
    {
      "eventName": "SKILL_ENABLED"
    },
    {
      "eventName": "SKILL_DISABLED"
    }
  ]
}
```
Example of skill schema with event subscription.
```json
{
  "manifest": {
    "apis": {
      "...":"..."
    },
    "events": {
      "subscriptions": [
        {
          "eventName": "SKILL_ENABLED"
        },
        {
          "eventName": "SKILL_DISABLED"
        }
      ],
      "regions": {
        "NA": {
          "endpoint": {
            "uri": "{lambdaExecutionRoleARN}"
          }
        }
      },
      "endpoint": {
        "uri": "{lambdaExecutionRoleARN}"
      }
    },
    "manifestVersion": "x.x",
    "permissions": [
      {"...":"..."}
    ],
    "privacyAndCompliance": {
      "...":"..."
      },
      "...":"..."
    },
    "...":"..."
  }
}

```

### 5. Configure, rebuild and redeploy Lambda function
1. Create `src/main/resources/onebusaway.properties` with the following parameters:

```
skill-app-id-development=
aws.key-id=
aws.secret-key=
googlemaps.api-key=
onebusaway.api-key=
```

Fill in the values:

`skill-app-id-development` is Alexa Skill ID from previous step

`aws.key-id` and `aws.secret-key` are `{appExecutionAccessKey}` and `{appExecutionSecretKey}` respectively as generated by CloudFormation

`googlemaps.api-key` Google Maps GeoCoding API Key which can be obtained from [Google Developers](https://developers.google.com/maps/get-started/)

`onebusaway.api-key` can be obtained from your local OBA region. Typically, you can use `TEST` while are you waiting for a key.

1. Build this project on the command line with `mvn package`.
1. Upload updated .jar to Lambda.

CAUTION: Every time you re-deploy to Lambda using the `lambda-maven-plugin`, you must
manually re-add "Alexa Skills Kit" as the function's _Trigger_. Support for Event Source configuration is coming.
For now, you do not need to do this if you deploy your code through the Lambda UI in AWS Console.

### Testing without an Alexa device

Don't have an Amazon Echo?  No worries, there are a few options.

1. [**Alexa Skill Testing Tool**](https://echosim.io/) - It simulates the Echo experience within your web browser (successfully tested with Firefox - Chrome and Microsoft Edge don't seem to work).
1. ~~[**Roger, with Alexa**](https://rogertalk.com/login?continue=%2Fauth%2Falexa) - [Roger](https://rogertalk.com/) is a free group voice messenger app, and it supports communicating with Alexa.  See [this Engadget article](https://www.engadget.com/2016/05/12/roger-app-puts-amazon-alexa-in-your-phone-for-free/) for details.~~ Roger [shut down](https://medium.com/roger-talk/what-weve-been-up-to-project-fika-79e4ee3d44f8#.lz2pnpy3c) on March 15th, 2017.
1. [Amazon App on iOS](http://www.macworld.com/article/3181810/ios/amazon-brings-alexa-to-its-main-ios-app-to-help-iphone-users-shop-and-track-orders.html) - You can now access Alexa skills via the Amazon iOS app.  See [this article](http://www.macworld.com/article/3181810/ios/amazon-brings-alexa-to-its-main-ios-app-to-help-iphone-users-shop-and-track-orders.html) for details.

Just remember to log in using the same Amazon account that you've used above to set up the skill.

## License
[Apache v2.0](http://www.apache.org/licenses/LICENSE-2.0)
