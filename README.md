liten
=====

Website for small reviews/criticism/summary.

# Overview

## URLs

* Main URL: ``http://127.0.0.1:8080/g/index``
* Health check (standard): ``curl -X POST http://127.0.0.1:8080/rest/health``
* Admin (standard): ``http://127.0.0.1:8080/g/admin/config``

Sample request with originating request ID ``oid``:

```
OID=`cat /dev/random | head -c 12 | base64` && echo "X-Oid = $OID" && curl -"X-Oid: $OID" -X POST -v http://127.0.0.1:8080/rest/health && echo
```

# Links

* [Brikar](https://github.com/truward/brikar)

