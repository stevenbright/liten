liten
=====

Website for small reviews/criticism/summary.

# How to build

You'll need:

* JDK 8
* maven
* nodejs (a.k.a. node) and npm (comes along with node)


# Overview

## URLs

* Main URL: ``http://127.0.0.1:8080/g/index``
* Health check (standard): ``curl -X POST http://127.0.0.1:8080/rest/health``
* Admin (standard): ``http://127.0.0.1:8080/g/admin/config``

Sample request with originating request ID ``oid``:

```
OID=`cat /dev/random | head -c 12 | base64` && echo "X-Oid = $OID" && curl -"X-Oid: $OID" -X POST -v http://127.0.0.1:8080/rest/health && echo
```

## Perf Test

Healthcheck (keep-alive behavior, concurrency=10, count of requests=100):

```
ab -k -n 100 -c 10 -m POST http://127.0.0.1:8080/rest/health
```

Login page:

```
ab -k -n 1000 -c 10 http://127.0.0.1:8080/g/login
```

## Sample Start

```
java -server -jar liten-website.jar -Dbrikar.settings.path=file:/yourconfigdir/brikar.properties -Dapp.logback.logBaseName=/yourlogdir/brikar.log -Dapp.logback.rootLogId=ROLLING_FILE
```

# Links

* [Brikar](https://github.com/truward/brikar)

