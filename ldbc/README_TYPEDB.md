# Notes on LDBC (for TypeDB)

Key components

* DATAGEN: ***Datagen generates data***
* DRIVER: ***Driver generates the parameters and update streams***
* IMPL: ***Driver Impls runs the queries on the databases***

## Installation

### Install Datagen
* set you JAVA_HOME to use java 11
  ***SPARK NEEDS JAVA 11*** !!!
* `conda activate datagen`
   * (in particular need to install dependencies:
     `pip install duckdb pytz networkit pandas==2.0.3`)
* build project as in README and set ALL the environment vars

### Install Driver
* Note there is an install dependencies script
* follow README

### Install Implementation
* build JAVA projects, e.g. `postgres/scripts/build.sh`
* set vars
```
export SF = 0.003
export LDBC_SNB_DATAGEN_DIR=/Users/cxdorn/Git/ldbc_snb_datagen_spark
export LDBC_SNB_DRIVER_DIR=/Users/cxdorn/Git/ldbc_snb_interactive_v2_driver
export DATA_INPUT_TYPE=parquet
export LDBC_SNB_DATAGEN_MAX_MEM=4g
```

## Data and parameters

The `IMPL/scripts/generate-scripts.sh` automates the below.

### Generate data
* `DATA_GEN/tools/run.py` ...

### Generate parameters
* `DRIVER/paramgen/paramgen.py`
* stores params in `DRIVER/parameters` (might wanna move this to `IMPL/parameters`)

### Generate update streams
* `DRIVER/scripts/create_update_streams.py`
* stores in `DRIVER/scripts/inserts` and `DRIVER/scripts/deletes` (might wanna move this to `IMPL/update-streams-SF`)

## Benchmarking Postgres

### Installation
* install psycopg with `pip install "psycopg[binary,pool]"` (see https://pypi.org/project/psycopg/)

### Loading data
* provide the generate data in `env` variable
* Run PostrgreSQL in docker (this is automatically done by the `start.sh` script)
* (Note if running postgres yourself: file sharing needs to be set use gRPC in Docker Preferences)

### Benchmarking
* Set the parameters and update_stream parameters in the `property` files to point to the right directories!
* In theory the README says to run
```
driver/create-validation-parameters.sh
driver/validate.sh
driver/benchmark.sh
```
As I understand you don't really need the first two steps, which are purely for validating loaded data vs parameters.

### Development
*

## TypeDB

### Installation

#### Mvn build
* From project root issue: `mvn [clean] package -DskipTests -Ptypeql`

#### Provide missing jni dependency
* from `pom` exclude `ldbc` driver
* From project root issue: `mvn dependency:copy-dependencies -Ptypeql`
* from `pom` include `ldbc` driver



## Cypher
* ... not done yet

## Read about LDBC

### Paper and thesis

* ... not done yet