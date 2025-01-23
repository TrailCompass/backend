# using multistage docker build
# ref: https://docs.docker.com/develop/develop-images/multistage-build/
    
# temp container to build using gradle
FROM gradle:alpine AS build
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME

COPY --chown=gradle:gradle . $APP_HOME/
USER root
RUN chmod +x $APP_HOME/gradlew
RUN cd $APP_HOME
COPY . .

RUN gradle server:clean server:build
RUN ls -lh $APP_HOME/server/build/libs
    
# actual container
FROM eclipse-temurin:21-jdk-alpine AS production
ENV APP_HOME=/usr/app
ENV PORT=8080
    
WORKDIR $APP_HOME
COPY --from=build $APP_HOME/server/build/libs/server-*.jar $APP_HOME/server.jar
RUN mkdir "/usr/app/plugins"

HEALTHCHECK --interval=5m --timeout=3s \
  CMD curl -f http://localhost:$PORT/health || exit 1

EXPOSE $PORT
VOLUME ["/usr/app/plugins"]
ENTRYPOINT ["java", "-jar", "server.jar"]