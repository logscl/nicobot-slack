# nicobot-slack [![Build Status](https://travis-ci.org/logscl/nicobot-slack.svg?branch=master)](https://travis-ci.org/logscl/nicobot-slack)
The (second) best bot in the world !

# run with docker

## build

open a console on the root then
`docker build -t nicobot:latest .`

This will create an image in your local docker repository. 

## run

Create an env file (located for instance in dev/prod.env) containing the following content:

```
api.uri=http://api.nicobot.zqsd.be
spring.profiles.active=master
nicobot.featured.channel=general
youtube.video.uri=https://youtu.be/
github.repository.username=logscl
github.repository.name=nicobot-slack
algorithmia.nudity.algorithm=algo://sfw/NudityDetectioni2v/0.2.4
slack.api.key=your-bot-api-key
search.cx.key=your-google-search-cx-key
search.api.key=your-google-search-key
github.api.key=your-github-key
nicobot.persitence.api.key=a-nicobot-persistence-key
``` 

(replace the last values with the right ones)

then run this command:

`docker run --env-file env/prod.env --rm -d --name nicobot nicobot:latest`

To check logs:
`docker logs -f nicobot`

To stop the container:
`docker stop nicobot`

# Run in development

Create a new configuration in your favorite IDE launching the main class `be.zqsd.nicobot.BotMain`.
Add environment variables, the same shown above in the docker section.

# TODO
* change slack api lib (https://github.com/slackapi/java-slack-sdk)
* upgrade java version