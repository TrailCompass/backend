# using multistage docker build
# ref: https://docs.docker.com/develop/develop-images/multistage-build/
    
# temp container to build using gradle
FROM gradle:alpine AS build
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
FROM eclipse-temurin:21-jdk-alpine AS production
ENV APP_HOME=/usr/app/
ENV PORT=8080
    
WORKDIR $APP_HOME
COPY --from=build $APP_HOME/build/libs/backend_java-*.jar $APP_HOME/backend_java.jar

HEALTHCHECK --interval=5m --timeout=3s \
  CMD curl -f http://localhost:$PORT/health || exit 1

RUN ls -la $APP_HOME

EXPOSE $PORT
ENTRYPOINT ["java", "-jar", "backend_java.jar"]