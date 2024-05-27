# LogN'Roll

a simple OTLP log store

![logo](./logo.png)

* Supports OTLP/HTTP (No Grpc)
* Compatible with both Protocol Buffers and JSON
* Gzip compression supported
* Stores log data in SQLite
* Full-Text search (trigram)
* Built-in UI

> [!INFO] This is a hobby project designed to facilitate the verification of log ingestion with OTLP. It is not intended for use in environments where reliability or high performance is required.

## Build and run

Java 21+ is required.

```
./mvnw clean package -DskipTests
java -jar target/lognroll-0.0.1-SNAPSHOT.jar
```

or

```
./mvnw spring-boot:run
```

* OTLP/HTTP endpoint: http://localhost:4318/v1/logs
* Default bearer token: `changeme` (you can change the token with `--lognroll.auth.token=verysecuretoken`)

## Run with Docker

```
docker run --rm -p 4318:4318 -v ./data:/data -e LOGNROLL_DB_PATH=/data/lognroll.db ghcr.io/making/lognroll:jvm
```

## Send a example record

```
cat src/test/resources/logs.json | curl -H "Content-Type: application/json" -H "Authorization: Bearer changeme" -s http://localhost:4318/v1/logs --data-binary @- -v
```

then, check the stored data

```
$ curl -s http://localhost:4318/api/logs -H "Authorization: Bearer changeme" | jq .
[
  {
    "logId": 1,
    "timestamp": "2018-12-13T14:51:00.300Z",
    "observedTimestamp": "2018-12-13T14:51:00.300Z",
    "severity": "Information",
    "serviceName": "my.service",
    "scope": "my.library",
    "body": "Example log record",
    "traceId": "e41f0414517bf7cd37f35d370f6ebd07adf7f35dc50bad02",
    "spanId": "104135f41ec40b70b5075ef8",
    "attributes": {
      "int.attribute": 10,
      "array.attribute": [
        "many",
        "values"
      ],
      "double.attribute": 637.704,
      "string.attribute": "some string",
      "map.attribute": {
        "some.map.key": "some value"
      },
      "boolean.attribute": true,
      "my.scope.attribute": "some scope attribute"
    },
    "resourceAttributes": {}
  }
]
```

Go to the web UI http://localhost:4318 (username: empty, password: `changeme` same as token)

<img width="1024" alt="image" src="https://github.com/making/lognroll/assets/106908/d0d11eea-cc13-4eb5-aa1f-119403157389">

## Send from OTEL Collector

```yaml
exporters:
  otlphttp/lognroll:
    endpoint: http://localhost:4318
    tls:
      insecure: true
    headers:
      Authorization: Bearer changeme
```
