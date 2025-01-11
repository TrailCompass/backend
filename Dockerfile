# using multistage docker build
# ref: https://docs.docker.com/develop/develop-images/multistage-build/
    
# temp container to build using gradle
FROM gradle:alpine AS TEMP_BUILD_IMAGE
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY build.gradle settings.gradle $APP_HOME
  
COPY gradle $APP_HOME/gradle
COPY --chown=gradle:gradle . /home/gradle/src
USER root
RUN chown -R gradle /home/gradle/src
    
RUN gradle build || return 0
COPY . .

RUN gradle clean build
    
# actual container
FROM eclipse-temurin:21-jdk-alpine as production
ENV ARTIFACT_NAME=backend_java-1.0-SNAPSHOT.jar
ENV APP_HOME=/usr/app/
ENV PORT=8080
    
WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .
    
EXPOSE $PORT
ENTRYPOINT exec java -jar ${ARTIFACT_NAME}