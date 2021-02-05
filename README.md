# Pathmap Storage Service
Pathmap Storage Service ...

## Prerequisite
1. jdk11
2. mvn 3.6.2+

## Configure services



## Try it

### 1. Start the database

You need a Cassandra to store the PathMap info. To ease the setup, we have provided a `docker-compose.yml` file which start a Cassandra  container and bind the network ports.

The database can be started using:
```
docker-compose up
```

Once the database is up you can start your Quarkus application.


### 2. Start gateway in debug mode
```
$ mvn quarkus:dev
```


