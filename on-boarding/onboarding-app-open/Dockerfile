FROM adoptopenjdk/openjdk11-openj9:alpine-jre
WORKDIR /app
COPY target/onboarding-app-open-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
