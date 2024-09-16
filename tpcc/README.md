# PyTPCC for TypeDB

## TPCC Benchmark

### Notes and changes

TODO


## Running the Benchmark locall on MacOS

### TypeDB

* Install `typedb`
* Start typedb with `typedb server`
* Install python driver: `pip install typedb`

See `launch.json` config for TPCC running options.

### Postgres

* Install `postgresql` (tested with `postgresql@14`)
* Start `postgresql` with
  * `initdb -D ~/data/`
  * `postgres -D ~/data`
  * in `~/data/postgresql.conf` set `wal_sync_method` to `fsync_writethrough`
* Install python driver: `pip install psycopg2-binary` 

Then see `launch.json` for launch configurations.

### MongoDB

* Install `mongod`.
* Start `mongod`.
  * With transactions

    ```
    mongod --replSet rs0 --bind_ip localhost --config /opt/homebrew/etc/mongod.conf
    ```
  * Without transactions


    ```
    mongod --bind_ip localhost --config /opt/homebrew/etc/mongod.conf
    ```
* Install python driver: `pip install pymongo`

### With transactions (see config)

```
mongod --replSet rs0 --bind_ip localhost --config /opt/homebrew/etc/mongod.conf
```

Then see `launch.json`.

Use flag `--no-load` if the database has already been created.

### Without transactions 

```
mongod --bind_ip localhost --config /opt/homebrew/etc/mongod.conf
```

Then see `launch.json`.

## Neo4j

* Install `neo4j 5`
* Start with `neo4j start`
* Install python driver: `pip install neo4j`

Then see `launch.json`. 

**Note**: when using the build in parallelization (`--clients=XX`) of the benchmark, data loading with Neo4j as **troubles**. Scale factor 10

