FROM eclipse-temurin:21-jdk-jammy as build
COPY target/*.jar /app/app.jar
WORKDIR /app

# optional: extract layers for layered jar optimization
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/ ./

ARG PROFILE=prod
ENV SPRING_PROFILES_ACTIVE=${PROFILE}

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
