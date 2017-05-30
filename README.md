liten
=====

Website for small reviews/criticism/summary.

# How to build

You'll need:

* JDK 8
* maven
  * More than 100m required to compile the project. Try using ``export MAVEN_OPTS=-Xmx512m`` before running ``mvn clean install`` if you're getting out-of-memory errors.
* nodejs (a.k.a. node) and npm (comes along with node)


# Overview

## URLs

### Generic

* Main URL: ``http://127.0.0.1:8080/g/index``
* Health check (standard): ``curl -X POST http://127.0.0.1:8080/api/health``
* Admin (standard): ``http://127.0.0.1:8080/g/admin/config``

Sample request with originating request vector ``RV``:

```
RV=`cat /dev/random | head -c 12 | base64` && echo "RV = $OID" && curl -"RV: $RV" -X POST -v http://127.0.0.1:8080/rest/health && echo
```

### Detail Page

Dev-only detail page:

* "English", the language: ``http://127.0.0.1:8080/g/cat/item/S1.X010``
* "Novel", the genre: ``http://127.0.0.1:8080/g/cat/item/S1.G010``
* "Leo Tolstoy", the author: ``http://127.0.0.1:8080/g/cat/item/S1.A010``
* "War and Peace", the book: ``http://127.0.0.1:8080/g/cat/item/S1.B210``

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

