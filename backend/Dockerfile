FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml ./

COPY . ./

CMD ["mvn", "spring-boot:run", "-Dspring-boot.run.profiles=dev", \
     "-Dspring.devtools.restart.enabled=true", \
     "-Dspring.devtools.livereload.enabled=true", \
     "-Dspring.devtools.remote.secret=mysecret"]
