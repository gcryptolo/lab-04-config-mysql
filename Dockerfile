FROM image-registry.openshift-image-registry.svc:5000/giovanni-manzone-dev/ubi8-openjdk-21:1.18

COPY target/lab-04-config-mysql-1.0.0.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]