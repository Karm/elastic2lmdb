## Elastic 2 LMDB
POC of a dumper from Elastic to LMDB

```
karm@local:~/Projects/rob/elastic2lmdb$ docker-compose -f ../sinkit/integration-tests/docker-compose.yml up elastic

TODO format:
key: 8byte crc64; value: {.... flags, accu, ...}
```
