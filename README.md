# Pathmap Storage Service
Pathmap Storage Service allows user to store, retrieve, and delete path-mapped files.

Each file has a path, e.g, '/foo/bar.txt', associated with it. All files are grouped 
by 'filesystem'. You may think of it as the different drivers on you disk.

Users can r/w the files via REST api. Different users can r/w same file without 
affecting each other. For example, if one user is downloading the '/foo/bar.txt' 
while another user is uploading a new version of it, the first user will get the 
file content at the moment the download started.

The most significant feature is the 'cluster' mode. You can deploy it on a Cloud platform, 
such as Openshift, and scale up as many nodes as you want. The concurrent r/w promise is 
still held without worrying about conflicts. On cluster mode, all nodes share the same 
persistent volume and connect to the same Cassandra as the backend DB. 

There are instructions at the bottom about how to set up a storage service cluster 
on Openshift.

## Prerequisite
1. jdk11
2. mvn 3.6.2+

## Prerequisite for debugging in local
1. docker 20+
2. docker-compose 1.20+

## Configure services

To make it run, Cassandra, Kafka, and Honeycomb(optional) need to be configured. Please see the example in `application.yaml`.

## Try it

### 1. Build (with jdk11+ and mvn 3.6.2+)
```
$ git clone git@github.com:Commonjava/pathmap-storage-service.git
$ cd indy-storage-service
$ mvn clean compile
```

### 2. Start the dependant service

It uses Cassandra as the backend DB. Below will start a standalone Cassandra server.
Make sure you have Docker installed.

```
docker-compose up
```

Once the database and message broker containers up you can start your Quarkus application.

### 3. Start in debug mode
```
$ mvn quarkus:dev
```

### 4. Verify the service running
By clicking http://localhost:8080/swagger-ui/

### 5. Open another terminal, upload a file, download it, and list a directory.
```
$ echo "test $(date)" | curl -X PUT -d @- http://localhost:8080/api/content/myfiles/foo/bar.txt
$ curl http://localhost:8080/api/content/myfiles/foo/bar.txt
$ curl http://localhost:8080/api/browse/myfiles/foo
```

## Scale up

The docker image is at https://quay.io/factory2/indy-storage-service:latest,
and you can run a cluster by deploying it on a cloud platform.

### TODO: Deploy on Openshift

