# LogN'Roll

a simple OTLP log store

![logo](./logo.png)

* Lightweight
* Supports OTLP/HTTP (No Grpc)
* Compatible with both Protocol Buffers and JSON
* Gzip compression supported
* Stores log data in SQLite3
* Built-in UI

## Build and run

Java 21+ is required.

```
./mvnw clean package -DskipTests
```

```
java -jar target/lognroll-0.0.1-SNAPSHOT.jar
```

* OTLP/HTTP endpoint: http://localhost:4318/v1/logs
* Default bearer token: `changeme` (you can change the token with `--lognroll.auth.token=verysecuretoken`)

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

Go to the web UI http://localhost:4318

<img width="1024" alt="image" src="https://github.com/making/lognroll/assets/106908/5864e65a-549f-41b0-81c2-0cfe6b397d36">

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
