# LogN'Roll

a simple log store

![logo](./logo.png)

* Lightweight
* Supports OTLP/HTTP (No Grpc)
* Compatible with both Protocol Buffers and JSON
* Gzip compression supported
* Stores log data in SQLite3

## Send a example record

```
cat src/test/resources/logs.json | curl -H "Content-Type: application/json" -s http://localhost:4318/v1/logs --data-binary @- -v
```
