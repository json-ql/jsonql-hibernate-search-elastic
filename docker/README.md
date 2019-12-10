# ElasticSearch docker

This directory contains ElasticSearch docker image definition for tests. 

## Starting

```bash
$ docker-compose up -d
Building elastic
...
Successfully built 34082369482c
Successfully tagged jsonql-hibernate-search-elastic:latest
WARNING: Image for service elastic was built because it did not already exist. To rebuild this image you must use `docker-compose build` or `docker-compose up --build`.
Creating jsonql-hibernate-search-elastic ... done
```

Check if everything works:

```bash
$ curl http://localhost:9200/_cat/indices
yellow open .watches                    a483N4JIQqWT1NQjtuXG8g 1 1  4 0 35.4kb 35.4kb
yellow open .monitoring-es-6-2019.12.10 i4Vo65S1SQav4k6huPFVAg 1 1 13 0 83.4kb 83.4kb
```

## Stopping

```bash
$ docker-compose down -v
Stopping jsonql-hibernate-search-elastic ... done
Removing jsonql-hibernate-search-elastic ... done
Removing network docker_default
```

With image cleanup:

```bash
$ docker-compose down -v --rmi all
Stopping jsonql-hibernate-search-elastic ... done
Removing jsonql-hibernate-search-elastic ... done
Removing network docker_default
Removing image jsonql-hibernate-search-elastic
```
