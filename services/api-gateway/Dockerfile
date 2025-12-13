FROM openjdk:21-ea-13-jdk-slim-bullseye as build
COPY target/*.jar /app/app.jar
WORKDIR /app

# optional: extract layers for layered jar optimization
RUN java -Djarmode=layertools -jar app.jar extract

FROM openjdk:21-ea-13-jdk-slim-bullseye
WORKDIR /app
COPY --from=build /app/ ./

ARG PROFILE=prod
ENV SPRING_PROFILES_ACTIVE=${PROFILE}

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
