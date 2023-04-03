# TODO

* Clean this readme :)
* Messages handling:
    * save self messages (via web client / nicobot ?)
    * save events ? which one ?
* Commands handling:
  * duel (new look ?)
  * gommette
  * hangman
* tests
* tests
* tests
* linter
* did I mention tests ?

users per channel:
- https://api.slack.com/methods/conversations.members (1 call per chan, necessary ?)

# How to create a "Slack Application" and plug the bot onto this App, a step-by-step guide

Note: it is recommended to create 2 apps: one for "production" and one for "dev"

* Go to https://api.slack.com/apps
* Click on "create a new app"
* Click on "From a file manifest"
* Select the workspace you want to set up the bot on
* Copy the `manifest.yml` file content present in the repo (`/resources`).
* Click on the "YAML" tab then past the content of the file
* Change the `display_information.name` and `features.bot_user.display_name` values (They can be changed later)
* Click next then create.
* FYI: In the "Basic Information" tab, you can:
  * Install your app
  * Retrieve the app keys (required to run the app - see below, the environment variables)
  * Install the app into the workspace (see below)
  * Create an app-level token (see below)
  * Change the display information (name, color, icon)
* FYI: In the "App Home" tab, you can:
  * Change the bot display name
* Under the "Basic Information" tab, click "Install to workspace", then "allow"
* Under the "Basic Information" tab, click "Generate Token and scopes"
  * Type a name for the token (i.e "Nicobot-webhook")
  * Select the scope "connections:write"
  * Click "generate". You will get a key for the "SLACK_WEBSOCKET_KEY" property
* Under the "OAuth & Permissions" tab, you will get the key for the "SLACK_API_KEY" property

**Always keep the manifest.yml file up to date!**

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

Add the following Environment Variables (ignore comments, they are here to help you):
```
# to post issues on github, under a specific repository belonging to a user (user/repo-name)
GITHUB_API_KEY=<a-user-github-api-key>
GITHUB_REPOSITORY_NAME=repo-name
GITHUB_REPOSITORY_USERNAME=user
# Nicobot will only execute some features on this channel
NICOBOT_FEATURED_CHANNEL=dev
# Where nicobot will save the messages/greetings/..
NICOBOT_PERSISTENCE_API_KEY=<persistence-api-key>
NICOBOT_PERSISTENCE_API_URI=<some url>
# Used for the Greetings job
NICOBOT_TIMEZONE_NAME=Europe/Brussels
# ChatGPT OpenAPI Key
OPENAI_API_KEY=<some-key>
# The bot should display the messages from and above from this level
QUARKUS_LOG_LEVEL=DEBUG
# The build of the bot will only include messages from and above this level
QUARKUS_LOG_MIN_LEVEL=DEBUG
# Google keys. Look up yourself :)
SEARCH_API_KEY=<google-search-key>
SEARCH_CX_KEY=<google-cx-key>
# Slack API Keys:
## This key can be found under the "OAuth & Permissions" tab of the Slack APP.
SLACK_API_KEY=<slack-api-key>
## This key can be found when creating an "app level token" of a Slack APP.
SLACK_WEBSOCKET_KEY=<slack-websocket-key>
# Of you want to report errors, a sentry URL. You can always skip this by using QUARKUS_LOG_SENTRY=false (or changing the application.properties file)
QUARKUS_LOG_SENTRY_DSN=<sentry-dsn-url>
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
