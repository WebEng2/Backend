#
# Build Step
#

FROM gradle:jdk17-jammy AS builder
WORKDIR /app
COPY ./gradle /app/gradle
COPY ./src /app/src
COPY ./build.gradle /app/build.gradle
COPY ./gradlew /app/gradlew
COPY ./settings.gradle /app/settings.gradle

RUN ./gradlew build --stacktrace

#
# Deploy Step
#

FROM openjdk:17-bullseye
COPY --from=builder /app/build /app/build
ENTRYPOINT ["java","-jar","/app/build/libs/backend-0.0.1-SNAPSHOT.jar"]
