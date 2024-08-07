# LogN'Roll

a simple OTLP log store

![logo](./logo.png)

* Supports OTLP/HTTP (No Grpc)
* Compatible with both Protocol Buffers and JSON
* Gzip compression supported
* Stores log data in SQLite
* Full-Text search (trigram)
* Built-in UI

> [!NOTE]
> This is a hobby project designed to facilitate the verification of log ingestion with OTLP. It is not intended for use in environments where reliability or high performance is required.

## Run with Pre-built Native Binaries

Lognroll offers pre-built binaries for x86_64-pc-linux, x86_64-apple-darwin, and aarch64-apple-darwin. If you are using one of these environments, downloading these binaries is the easiest way to get started.

* For x86_64-pc-linux
```
curl -sL -o lognroll https://github.com/making/lognroll/releases/download/tip/lognroll-x86_64-pc-linux 
```
* For x86_64-apple-darwin (Intel Mac)
```
curl -sL -o lognroll https://github.com/making/lognroll/releases/download/tip/lognroll-x86_64-apple-darwin
```
* For aarch64-apple-darwin (Apple Silicon)
```
curl -sL -o lognroll https://github.com/making/lognroll/releases/download/tip/lognroll-aarch64-apple-darwin
```

After downloading the appropriate binary, you can start lognroll with the following commands:

```
mkdir -p data
chmod +x lognroll
./lognroll --lognroll.db.path=./data/lognroll.db --server.port=4318
```

* Web UI: http://localhost:4318 (username: empty, password: same as token bellow)
* OTLP/HTTP endpoint: http://localhost:4318/v1/logs
* Default bearer token: `changeme` (you can change the token with `--lognroll.auth.token=verysecuretoken`)

## Run with Docker

Run with the native image version. It starts up quickly (usually in less than a second), but it does not currently work on Arm environments such as Apple Silicon.

```
mkdir -p data
docker run --rm -p 4318:4318 -v ./data:/data -e LOGNROLL_DB_PATH=/data/lognroll.db ghcr.io/making/lognroll:native
```

Or the JVM version will work in any environment.

```
mkdir -p data
docker run --rm -p 4318:4318 -v ./data:/data -e LOGNROLL_DB_PATH=/data/lognroll.db ghcr.io/making/lognroll:jvm
```

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

For Native Image Build, run the following command. The native image (`./target/lognroll`) created here should work on the environment where it was built, including Arm.

```
./mvnw -Pnative -DskipTests native:compile
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
    "severityText": "Information",
    "severityNumber": 10,
    "serviceName": "my.service",
    "scope": "my.library",
    "body": "Example log record",
    "traceId": "e41f0414517bf7cd37f35d370f6ebd07adf7f35dc50bad02",
    "spanId": "104135f41ec40b70b5075ef8",
    "traceFlags": 0,
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

Clear the `from` field since the sample log is too old and click the "View Logs" button, you'll see the search result as below: 

<img width="1024" alt="image" src="https://github.com/making/lognroll/assets/106908/d3513ca2-825a-4229-8dc2-fe4f02460ef6">

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
