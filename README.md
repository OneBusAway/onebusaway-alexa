# OneBusAway skill for Alexa

Are you ready to ask about your bus with just your voice?

This project is an implementation of the [OneBusAway](http://onebusaway.org/) _open-source platform for real-time transit info_ for Amazon's [Alexa Voice Service](https://developer.amazon.com/public/solutions/alexa/alexa-voice-service).

## Install and Use

This project isn't released publicly yet.  We plan to make the first release around January 2016.

Initial spoken setup: TBD

Supported voice commands: TBD

## Develop

1. Install the [Java Platform SE Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Maven](https://maven.apache.org/).
2. Clone this repository.
3. Write some code and tests, then test your creation with `mvn test`.
4. Build this project on the command line with `mvn package`.  Look for "BUILD SUCCESS". Resulting JAR is `target/onebusaway-alexa-1.0-jar-with-dependencies.jar`, which you can now upload to AWS Lambda.
5. To upload to AWS Lambda in a semi-automated way, use `mvn lambduh:deploy-lambda` ([plugin homepage](https://github.com/SeanRoy/lambduh-maven-plugin)) with the necessary arguments. (The plugin will tell you what's missing.)

## Authors
* [Sean Barbeau](https://github.com/barbeau)
