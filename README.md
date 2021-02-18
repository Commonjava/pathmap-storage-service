# Pathmap Storage Service
Pathmap Storage Service is a single full-functional service for indy filesystem management, including file list, reading, writing and deleting.

## Prerequisite
1. jdk11
2. mvn 3.6.2+

## Configure services

To make it run, Cassandra, Kafka, and Honeycomb(optional) need to be configured. Please see the example in `application.yaml`.

## Try it

### 1. Start the database and the message broker

It needs a Cassandra to store the PathMap info as well as the message broker(Kafka) for event handler. To ease the setup, we have provided a `docker-compose.yml` file which start the containers and bind the network ports.

```
docker-compose up
```

Once the database and message broker containers up you can start your Quarkus application.

### 2. Start gateway in debug mode
```
$ mvn quarkus:dev
```

### 3. Verify the installation 

```
http://localhost:8080/swagger-ui/
```



