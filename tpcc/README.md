# PyTPCC for TypeDB

Based on Andy Pavlov's great implementation of the benchmark.

## About the benchmark

Data size (can be configured in `constants` file)
* `n` Warehouses
* 10 Districts per warehouse
* 3000 Customers per district
* 3000 Inital order per district
* Many items...

The **scalefactor** scales this data (higher scale factor = less data, e.g., `--scalefactor=100` means 30 customers per district)

There are 5 workloads (T1... T5):
* 45% of the time: T1 creates new orders
* 4% of the time: T2 records a delivery
* 4% of the time: T3 queries order status
* 43% of the time: T4 records a payment
* 4% of the time: T5 queries stock level

## Running the benchmark

### TypeDB 3

#### Set up

* Install TypeDB or run from source
  ```
  cargo run --package typedb_server_bin --bin typedb_server_bin --release
  ```
* Install python driver: `pip install typedb-driver==3.XXX`   

#### Load data

* Run typedb
* Run the data loading:

  ```
  tpcc.py --warehouses=X --scalefactor=100 --no-execute --reset --clients=Y typedb
  ```

**Note** Number of clients shouldn't be larger than number of warehouses (data is sharded by warehouse).

#### Query the data

* Run typedb
* Run the workloads: 

  ```
  tpcc.py --warehouses=X --scalefactor=100 --no-load --clients=Y typedb
  ```

### TypeDB 2

#### Set up

* Install TypeDB 2.28+
* Install python driver: `pip install typedb==2.XXX`

#### Load data

* Run typedb
* Run the data loading:

  ```
  tpcc.py --warehouses=X --scalefactor=100 --no-execute --reset --clients=Y typedb2
  ```

#### Query the data

* Run typedb
* Run the workloads:

  ```
  tpcc.py --warehouses=X --scalefactor=100 --no-load --clients=Y typedb2
  ```

### Postgres

#### Install

* Install `postgresql` (tested with `postgresql@14`)
* Install python driver: `pip install psycopg2-binary` 

#### Configure and run

* Start `postgresql` with
  * `initdb -D ~/data/`
  * `postgres -D ~/data`
  * in `~/data/postgresql.conf` set `wal_sync_method` to `fsync_writethrough`

#### Benchmark

As before, replace `typedb` by `postrgres`

### MongoDB

#### Install

* Install `mongod`.
* Install python driver: `pip install pymongo`

#### Run

* Start `mongod`.
  * With transactions

    ```
    mongod --replSet rs0 --bind_ip localhost --config /opt/homebrew/etc/mongod.conf
    ```
  * Without transactions

    ```
    mongod --bind_ip localhost --config /opt/homebrew/etc/mongod.conf
    ```

#### Benchmark

As before.

### Neo4j

#### Install

* Install `neo4j 5`
* Install python driver: `pip install neo4j`

#### Run

* Start with `neo4j start`

#### Benchmark

As before.

**Note**: when using the build in parallelization (`--clients=XX`) of the benchmark, data loading with Neo4j has trouble. The fix is similar to that in TypeDB3 driver (using the `item_complete` event) 