# Notes on LDBC (for TypeDB)

This is the Driver Implementation (IMPL), one of three key components

1.  DATAGEN: ***This generates data*** (we assume this is cloned to `~/Git/ldbc_snb_datagen_spark`)
1.  DRIVER: ***This generates the parameters and update streams and runs the workload*** (we assume this is cloned to `~/Git/ldbc_snb_interactive_v2_driver`)
1.  IMPL: ***This provides the DRIVER with the implementations of the queries for the specific database to be benchmarked*** (this is the project you are looking at right now)

## Installation

### Install Datagen
* set you JAVA_HOME to use java 11
  (Spark needs Java 11)
* Ensure the following are installed:
     `pip install duckdb pytz networkit pandas==2.0.3` (there might be more dependencies)
* build project as in README and set ALL the environment vars
<!-- `conda activate datagen` -->

### Install Driver
* Note there is an install dependencies script
* Follow README to build the project

### Install Implementation
* Build JAVA projects, e.g. `postgres/scripts/build.sh`
* Set the following vars

```
export SF=0.003 \
LDBC_SNB_DATAGEN_DIR=~/Git/ldbc_snb_datagen_spark \
LDBC_SNB_DRIVER_DIR=~/Git/ldbc_snb_interactive_v2_driver \
DATA_INPUT_TYPE=parquet \
LDBC_SNB_DATAGEN_MAX_MEM=4g
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
* If not built, yet: `postgres/scripts/build.sh`
* install psycopg with `pip install "psycopg[binary,pool]"` (see https://pypi.org/project/psycopg/)

### Loading data
* provide the generated data folder as `POSTGRES_CSV_DIR` in environment (see README)
```
export POSTGRES_CSV_DIR=${LDBC_SNB_DATAGEN_DIR}/out-sf${SF}/graphs/csv/bi/composite-merged-fk/
```

* Run PostrgreSQL in docker (this is automatically done by the `start.sh` script)
* (Note if running postgres yourself: file sharing needs to be set use gRPC in Docker Preferences)

### Benchmarking
* Set the parameters and `update_stream` parameters in the `property` files to point to the right directories!
* In theory the README says to run
```
driver/create-validation-parameters.sh
driver/validate.sh
driver/benchmark.sh
```
As I understand you don't really need the first two steps, which are purely for validating loaded data vs parameters.

### IntelliJ Development
* Select Java SDK in Project Structure settings
* Ensure Postgres is running. `docker run --name some-other-postgres -e POSTGRES_PASSWORD=mysecretpassword -p 5432:5432 -d postgres` should to the job but there is also a `docker-compose.yml` provided (run with `docker-compose up -d` after setting required params)
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