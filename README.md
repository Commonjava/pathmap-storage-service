# Pathmap Storage Service
Pathmap Storage Service (PMS) allows you to store, retrieve, and delete 'path-mapped' files.

More specifically, each file (or 'logical file') in Pathmap storage has a path, e.g, '/foo/bar.txt'. The service uses a Cassandra path DB to isolate logical file access from the physical file access. When reading a file, it first queries the path DB for the physical file location then read the file. When writing a file, it first writes the file to a random physical location, then update the path DB to map the path to the new physical location.

Files are grouped by 'filesystem'. A filesystem is like a driver on a disk. You can use as many filesystems as you want, or just use a single filesystem for all files. The filesystem gives you some statistics numbers as to the count of files, total size, etc. We may add more controls per each filesystem in the future, such as access permission. 

Users r/w the files via REST api. Multiple users can r/w same file without affecting each other. For example, if one user were to download the '/foo/bar.txt' while another user is uploading a new version of it, the first user would get the integral file content at the moment the download starts.

The concurrent r/w is no surprise when you run a standalone service which only concerns one node. The most significant fact lies on the 'cluster' mode. You can deploy it on a Cloud platform, such as k8s or Openshift, and scale up to as many nodes as you want. The concurrent r/w promise still hold. On cluster mode, all nodes share the same persistent volume and connect to the same Cassandra as the backend DB. 

## Prerequisite
1. jdk11
2. mvn 3.6.2+

## Prerequisite for debugging in local
1. docker 20+
2. docker-compose 1.20+

## Configure services
The basic configurations are storage 'baseDir' where the physical files are located, and the Cassandra connection properties. Refer to sample config in `./sample/config/application.yaml`.

## Try it

### 1. Get the source code
```
$ git clone git@github.com:Commonjava/pathmap-storage-service.git
$ cd indy-storage-service
```

### 2. (Optional) Build
If you only want to run pre-built Docker images, go directly to next step.
```
$ mvn clean compile
```

### 3. Start the services
Below will start standalone Pathmap storage service and its dependants. 

**NOTE**: Make sure you have Docker installed and the demon started.

```
docker-compose up -f docker-compose-all.yml
```

### 4. Verify the service running
By clicking http://localhost:8080

### 5. Test
Open another terminal, upload a file, download it, or list a directory.
```
$ echo "test $(date)" | curl -X PUT -d @- http://localhost:8080/api/storage/content/myfiles/foo/bar.txt
$ curl http://localhost:8080/api/storage/content/myfiles/foo/bar.txt
$ curl http://localhost:8080/api/storage/browse/myfiles/foo
```

## Development
For developers, you can start in **debug** mode.
```
$ docker-compose up
$ mvn quarkus:dev
```

## Cluster

The pre-built docker image is at https://quay.io/factory2/pathmap-storage-service:latest.
You can run the **cluster mode** by deploying it on a cloud platform.

### Deploy on Openshift

#### 1. Deploy Cassandra

First you need a Cassandra pod running. The simplest way is to create a Deployment, pick up the right image:tag with some necessary information such as labels and names, and start it up. 

You may also want to create a PVC and mount it to '/var/lib/cassandra' so that the data can be persistent between reboots. If you need, refer to Openshift and Cassandra docs for more details.

Then you create an Openshift 'Service' to expose the Cassandra port 9042. This port is used by storage service to connect to it. 

#### 2. Deploy storage service

2.1 Create a PVC with '**ReadWriteMany**' access mode

This is used to store the physical files. It can be 1G ~ 100G+ according to your use case. You can do this via Ocp UI.

2.2 Create ConfigMap 'pathmap-storage-service-config' to hold the config

You may use the './sample/config/application.yaml' as template. First, set the **cassandra** host, user/pass to the right values. After installation, Cassandra creates a default user/pass as "cassandra". If you've done nothing special, just use the default user/pass. 

Then replace **baseDir**'s value with '/opt/pathmap-storage-service/storage' (this is where we mount the PVC). And run below command. 

```
$ oc create configmap pathmap-storage-service-config \
    --from-file=./sample/config/application.yaml
```

2.3 Create DeploymentConfig

Open './sample/ocp/deploymentconfig-pathmap-storage-service.yaml' and replace the "{{ persistentVolumeClaim }}" with the PVC name you created in the first step, and run below command. This will by default start 2 pods which would work as a cluster. 

```
$ oc create -f ./sample/ocp/deploymentconfig-pathmap-storage-service.yaml
```

2.4 Create the Ocp Service

```
$ oc create -f ./sample/ocp/service-pathmap-storage-service.yaml
```

2.5 Create a Route to expose the service, via Ocp UI.