# TODO

* Write an how-to create a slack app
* nicobot methods:
  * send file
* Clean this readme :)
* Messages handling:
    * save self messages (via web client / nicobot ?)
    * save events ? which one ?
* Commands handling:
  * Duel
  * duel (new look ?)
  * GitHub issue
  * gommette
  * hangman
  * admin say
  * top hgt
* hgt job
* tests
* tests
* tests
* did I mention tests ?
* docker build (hub ?)

users per channel:
- https://api.slack.com/methods/conversations.members (1 call per chan, necessary ?)


# nicobot-slack Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

Add the following Environment Variables:
```
NICOBOT_FEATURED_CHANNEL=dev
QUARKUS_LOG_LEVEL=DEBUG
QUARKUS_LOG_MIN_LEVEL=DEBUG
SEARCH_API_KEY=google apis key
SLACK_API_KEY=slack api key for the integration
SLACK_WEBSOCKET_KEY=slack websocket api key for the integration
```

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/nicobot-slack-3.0.0-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.
