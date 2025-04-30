FROM default-route-openshift-image-registry.apps.rm1.0a51.p1.openshiftapps.com/giovanni-manzone-dev/ubi8-openjdk-21

COPY target/lab-04-config-mysql-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]