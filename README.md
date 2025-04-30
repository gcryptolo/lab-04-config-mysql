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
5. Create DockerFile  
6. Create Helm chart for our application
7. Create Openshift pipeline to deploy our application
8. Configure Webhook to trigger pipeline on push
9. Test the pipeline
10. Test the application
11. Use ConfigMap and Secret to define connection parameter for the app
12. Test the application with ConfigMap and Secret

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

## 5. Create DockerFile 

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

## Create  Helm Chart

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

Helm is a package manager for Kubernetes that allows you to define, install, and manage Kubernetes applications. It uses a packaging format called charts, which are collections of files that describe a related set of Kubernetes resources.

The template directory contains YAML template you can populate with values from the values.yaml file. (check helm sintax to understand how to use it).
The Chart.yaml file contains metadata about the chart, such as its name, version, and description. The values.yaml file contains default values for the templates in the chart. You can override these values when you install the chart by providing your own values file or using command-line flags.
The values.yaml file is already populated with the connection parameters for the mysql DB, you can change them if you want.
You need to change the image repository in the deployment.yaml file to your image registry.


PS you can create the helm chart using the command:

```bash
helm create <your_chart_name>
```
it creates a directory with the same structure as above, you can then modify the files to fit your needs.

## 6. Create Openshift pipeline to deploy our application

Now its time to create the Openshift pipeline to deploy our application, you can find the pipeline in the pipeline directory.

Openshift offer a tekton technology to create and run pipelines, a pipeline is a set of task that are executed in order to build, test and deploy your application.
Every Task work on a shared workspace, so you can pass data from one task to another.


We have to understand the logic flow started from GIT Webhook to final deployment of our application and how all components work together.

To create all this resource we can use the add button at the top right corner of the console and select the resource we want to create, select import YAML and copy the YAML code in the file you want to create.




***1- EventListener***

The EventListener is a Tekton resource that listens for events from a webhook and triggers a pipeline run when an event is received. 


```bash
apiVersion: triggers.tekton.dev/v1beta1
kind: EventListener
metadata:
  name: lab-04-webhook-listener
  namespace: <your_name_space>>
spec:
  serviceAccountName: pipeline
  triggers:
    - name: git-trigger
      interceptors:
        - ref:
            name: "github"
          params:
            - name: "eventTypes"
              value:
                - "push"
      bindings:
        - ref: lab-04-trigger-binding
      template:
        ref: lab-04-trigger-template
```

Remember to change the name space to your name space, you can find it in the top right corner of the console.

The EventListener defines two ref :
1. bindings
2. template

Those are references to the TriggerBinding and TriggerTemplate resources that define the parameters to be passed to the pipeline run and the template for the pipeline run.




***2- TriggerTemplate***

The TriggerTemplate is a Tekton resource that defines the template for the pipeline run. It specifies the pipeline to be run and the parameters to be passed to the pipeline.

```bash
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: shared-pvc
  namespace: <your-namespace>
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi

---

apiVersion: triggers.tekton.dev/v1beta1
kind: TriggerTemplate
metadata:
  name: lab-04-trigger-template
  namespace: <your-namespace>
spec:
  params:
    - name: repo-url
    - name: revision
  resourcetemplates:
    - apiVersion: tekton.dev/v1beta1
      kind: PipelineRun
      metadata:
        generateName: lab-04-pipeline-run-
      spec:
        pipelineRef:
          name: lab-04-pipeline
        params:
          - name: repo-url
            value: $(params.repo-url)
          - name: revision
            value: $(params.revision)
        workspaces:
          - name: shared-workspace
            persistentVolumeClaim:
              claimName: shared-pvc
```

In this YAML we create first a PVC for operation of the pipeline, this is a shared volume that can be used by all tasks in the pipeline. 
Then we define the TriggerTemplate that will be used to create the PipelineRun when an event is received. The TriggerTemplate defines the parameters to be passed to the pipeline run and the template for the pipeline run.


***3- TriggerBinding***

The TriggerBinding is a Tekton resource that defines the mapping between the event payload and the parameters to be passed to the pipeline run. It specifies how to extract the parameters from the event payload.

```bash
apiVersion: triggers.tekton.dev/v1beta1
kind: TriggerBinding
metadata:
  name: lab-04-trigger-binding
  namespace: <your-namespace>
spec:
  params:
    - name: repo-url
      value: $(body.repository.clone_url)
    - name: revision
      value: $(body.ref)
```

The TriggerBinding defines the parameters to be passed to the pipeline run and how to extract them from the event payload. In this case, we are extracting the repository URL and the revision from the event payload.


4- Pipeline
The Pipeline is a Tekton resource that defines the steps to be executed in the pipeline run. It specifies the tasks to be executed and the order in which they are executed.

```bash
apiVersion: tekton.dev/v1beta1
kind: Pipeline
metadata:
  name: lab-04-pipeline
  namespace: <your-namespace>
spec:
  params:
    - name: repo-url
      description: The URL of the Git repository
    - name: revision
      description: The Git revision to check out
    - name: image-name
      default: image-registry.openshift-image-registry.svc:5000/giovanni-manzone-dev/lab-04-config-mysql:1.0.0
    - name: helm-release
      default: app-release
    - name: helm-chart-path
      default: ./helm
    - name: namespace
      default: giovanni-manzone-dev
  workspaces:
    - name: shared-workspace
  tasks:
    - name: fetch-repository
      taskRef:
        name: git-clone
      params:
        - name: url
          value: $(params.repo-url)
        - name: revision
          value: $(params.revision)
      workspaces:
        - name: output
          workspace: shared-workspace

    - name: maven-build
      taskRef:
        name: maven
      params:
        - name: GOALS
          value: clean package
        - name: MAVEN_IMAGE
          value: image-registry.openshift-image-registry.svc:5000/openshift/ubi8-openjdk-21:1.18
      workspaces:
        - name: source
          workspace: shared-workspace
      runAfter:
        - fetch-repository

    - name: docker-build-and-push
      taskRef:
        name: buildah
      params:
        - name: IMAGE
          value: $(params.image-name)
        - name: CONTEXT
          value: $(workspaces.shared-workspace.path)
        - name: TLSVERIFY
          value: "false"
      results:
        - name: built-image
          description: The name of the built image
      workspaces:
        - name: source
          workspace: shared-workspace
      runAfter:
        - maven-build

    - name: update-values
      taskSpec:
        steps:
            - name: update-image-tag
              image: registry.access.redhat.com/ubi9/ubi-micro
              script: |
                #!/bin/bash
                set -e
                sed -i "s|image:.*|image: $(params.image-name)|" $(workspaces.source.path)/helm/values.yaml
              workingDir: $(workspaces.source.path)
      params:
        - name: image-name
          value: $(params.image-name)
      workspaces:
        - name: source
          workspace: shared-workspace
      runAfter:
        - docker-build-and-push
    - name: helm-release
      taskRef:
        name: helm-upgrade-from-source
      params:
        - name: helmRelease
          value: $(params.helm-release)
        - name: helmChart
          value: $(params.helm-chart-path)
        - name: namespace
          value: $(params.namespace)
        - name: values
          value: |
            image: $(params.image-name)
      workspaces:
        - name: source
          workspace: shared-workspace
      runAfter:
        - update-values        -
        
```

NB  - Task
The Task is a Tekton resource that defines the steps to be executed in the task. 
It specifies the commands to be executed and the parameters to be passed to the commands,
in this example we define one task to update the value of the image created in docker-build-and-push task. 
You can feate a simple Resource Task and after refer it in the pipeline as the other task in the pipeline. Those task are predefined in Openshift and you can find them in the
Openshift documentation [Tekton Tasks](https://docs.openshift.com/container-platform/4.12/cicd/pipelines/tekton-tasks.html).





Lets esplore whats Openshift create for us:

When we create the EventListener it creates a service:

![img_6.png](doc%2Fimg%2Fimg_6.png)

This service is used to expose the EventListener to the outside world.

to access the EventListener from outside Openshift (Git Webhook) we have to expose this service with a route:

```bash
oc expose service el-lab-04-event-listener --port=8080 --name=lab-04-webhook-listener
```
check the route created:

![img_7.png](doc%2Fimg%2Fimg_7.png)


retrive the URL of the route from console or typing:

```bash
oc get route lab-04-webhook-listener -o jsonpath='{.spec.host}'
```


### 8. Configure Webhook to trigger pipeline on push

Now we havo to configure the webhook to trigger the pipeline on push, go to your GitHub repo and select Settings -> Webhooks -> Add webhook.




oc adm policy add-scc-to-user restricted-v2 -z pipeline -n giovanni-manzone-dev



##################################################################################

Now we will create a pipeline from console:

***Create repository***

From console in pipeline menu select Create and select Repository:

![img_3.png](doc%2Fimg%2Fimg_3.png)



add Git info to your repository:

![img_4.png](doc%2Fimg%2Fimg_4.png)

now we can see the repo added and the following screen message:

![img_5.png](doc%2Fimg%2Fimg_5.png)

Now openshift is telling us if we put a fila called push.yaml into repo it will create a pipeline for us, so lets create this file and push it to origin.

I'll show you how