FROM docker.io/library/ubi8-openjdk-21:latest

COPY target/lab-04-config-mysql-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]