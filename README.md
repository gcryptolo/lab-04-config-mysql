# lab-04-config-mysql
LAB-04 Deploy Java APP with DB connection provided by config


## Prerequisites
- Familiarity with Docker/Podman
- Familiarity with OpenShift
- RedHat Account and Sandbox Access  [OpenShift Sandbox](https://developers.redhat.com/developer-sandbox)
- GitHub account
- OC CLI installed
- Helm installed


## Objectives

- Create a simple java app that connects to a MySQL database to retrieve data
- Create mysql deploy on Openshift
- Deploy our application using helm and Openshift pipeline
- Use ConfigMap and Secret to define connection parameter for the app

## Steps

1. Clone Project from GitHub https://github.com/gcriptolo/lab-04-config-mysql.git
2. Create a new repo on your GitHub account
3. Copy the content of cloned project into your repo and push the code to origin (also local test)
4. Create mysql deployment using oc cli
5. Create DockerFile and Helm chart for our application
6. Use ConfigMap and Secret to define connection parameter for the app
7. Create Openshift pipeline to deploy our application
8. Configure Webhook to trigger pipeline on push
9. Test the pipeline
10. Test the application

## 1. Clone Project
```bash
 git clone https://github.com/gcriptolo/lab-04-config-mysql.git
```

## 2. Create a new repo on your GitHub account

- Go to your GitHub account and create a new repo by clicking on the "+" icon in the top right corner and selecting "New repository".
- Name your repo "lab-04-config-mysql" and select "Public" .
- Click "Create repository" button.
- clone your repo in local.

## 3. Copy the content of cloned project into your repo and push the code to origin (also local test)

From directory where you clone this project

```bash

cp -r lab-04-config-mysql/* <your_repo_path>/lab-04-config-mysql

cd <your_repo_path>/lab-04-config-mysql

```

### Test the application locally

To test the aplication locally you need to have docker or podman installed.

For podman you can run the following command to build the image and run the container:

```bash
podman run -d   --name mysql-container   -e MYSQL_ROOT_PASSWORD=ABC-XXX-123   -e MYSQL_DATABASE=testdb  -e MYSQL_USER=mysql_user -e MYSQL_PASSWORD=mysql_password -p 3306:3306 mysql:8.0
```

As you can see we expose mysql 8 DB on port 3306 and set the root password to ABC-XXX-123 and create a database called testdb with user mysql_user and password mysql_password.

### Test the application locally

insiede project in the application.yaml file we can find the connection parameter as:

```yaml

spring:
  main:
    banner-mode: console
  application:
    name: lab-04-config-mysql
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
```

So now we have to set this environment variable in our local IDE to test the application.

This can be done setting startup parameter or env variable like in  IntelliJ or lounch.json file in VSCode.


### Test the application locally

Start your application using your IDE and a test_table with its data is created for us.
If you go to https://localhost:8080/test-data/ with Podman or withyour browser you can see: 
![img.png](doc%2Fimg%2Fimg.png)

## 4. Create mysql deployment using oc cli

Now we have to create all resource we need on Openshift, lets create a mysql DB:

```bash
oc new-app mysql:8.0 --name mysql-container --env MYSQL_ROOT_PASSWORD=ABC-XXX-123 --env MYSQL_DATABASE=testdb --env MYSQL_USER=mysql_user --env MYSQL_PASSWORD=mysql_password
```

This command will create a new deployment with the name mysql-container and set the environment variables for the root password, database name, user and password as we done for podman before, note Openshift create  also the service associated.

![img_1.png](doc%2Fimg%2Fimg_1.png)

## 5. Create DockerFile and Helm chart for our application

### DockerFile

Remember change image repository to your image registry, you can find it in ImageStream tab of the consolle or typing:

```bash
oc get is -n openshift
```

This is my Dockerfile:

```dockerfile
FROM default-route-openshift-image-registry.apps.rm1.0a51.p1.openshiftapps.com/openshift/ubi8-openjdk-21:1.18

COPY target/lab-04-config-mysql-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Helm Chart

The helm chart is already created for you, you can find it in the helm directory.

The directory structure is:

```bash
helm/
├── Chart.yaml
├── values.yaml
├── templates/
    ├── deployment.yaml
    ├── service.yaml
    ├── route.yaml
```