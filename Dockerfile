FROM maven:3.6.3-jdk-11 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package -Pmaster

FROM adoptopenjdk:14.0.1_7-jre-hotspot
WORKDIR /usr/app
COPY --from=build /usr/src/app/target/dependency /usr/app/libs
COPY --from=build /usr/src/app/target/nicobot-slack-*.jar /usr/app/nicobot-slack.jar
CMD ["java", "-cp", "libs/*:nicobot-slack.jar", "be.zqsd.nicobot.BotMain"]