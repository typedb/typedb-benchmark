# Getting the benchmark to run

You are in the driver Implementation (IMPL) repo. The IMPL is one of three key components

1.  DATAGEN: See repo `lbdc/ldbc_snb_datagen_spark`. _This generates data_ (we assume this is cloned to `~/Git/ldbc_snb_datagen_spark`)
1.  DRIVER: See repo `ldbc/ldbc_snb_interactive_v2_driver`. _This generates the parameters and update streams and runs the workload_ (we assume this is cloned to `~/Git/ldbc_snb_interactive_v2_driver`)
1.  IMPL: _This provides the DRIVER with the implementations of the queries for the specific database to be benchmarked_

## Installation

### DATAGEN
* set you JAVA_HOME to use java 11
  (Spark needs Java 11)
* Ensure the following are installed:
     `pip install duckdb pytz networkit pandas==2.0.3` (there might be more dependencies)
* build project as in README and set ALL the environment vars
<!-- `conda activate datagen` -->

### DRIVER
* Note there is an install dependencies script
* Follow README to build the project

### IMPL
* Build JAVA projects, e.g. `postgres/scripts/build.sh`
* Set the following vars

    ```
    export SF=0.003 \
    LDBC_SNB_DATAGEN_DIR=~/Git/ldbc_snb_datagen_spark \
    LDBC_SNB_DRIVER_DIR=~/Git/ldbc_snb_interactive_v2_driver \
    DATA_INPUT_TYPE=parquet \
    LDBC_SNB_DATAGEN_MAX_MEM=4g
    ```

    (You can set SF to one of the available scaling factors)
    
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

### Benchmarking

* Run `typeql/driver/benchmark.sh`
* The file references `benchmark-sfXXX.properties` (replace with which ever property file you wish to use)
* Ensure the right queries are toggled in the properties file

### Development

* `QueryType` holds a dictionary of query label to file name prefixes.
* `QueryStore` contains useful methods, referring to both query operations and query types (so both need to be available) 
* `LdbcOperation` is the superclass of specific operations, defining e.g. which parameters this operations take
  * e.g. `LdbcQuery1` 
  * e.g. `LdbcQuery1Result`
  * e.g. `LdbcInsert1AddPerson`
  * e.g. `LdbcDelete1RemovePerson`
* For each operation, use `registerOperationHandler` to register a TypeQL class that will handle the operation

## Cypher

... not done yet, but similar

## Driver user guide

### Inputs

The benchmark framework relies on the following inputs produced by the [SNB Datagen](https://github.com/ldbc/ldbc_snb_datagen_hadoop/):

* **Initial data set:** the SNB graph in CSV format (`social_network/{static,dynamic}`)
* **Update streams:** the input for the update operations (`social_network/updateStream_*.csv`)
* **Substitution parameters:** the input parameters for the complex queries. It is produced by the Datagen (`substitution_parameters/`)

### Driver modes

For each implementation, it is possible to perform to perform the run in one of the [SNB driver's](https://github.com/ldbc/ldbc_snb_interactive_v1_driver) three modes: create validation parameters, validate, and benchmark.
The execution in all three modes should be started after the initial data set was loaded into the system under test.

1. Create validation parameters with the `driver/create-validation-parameters.sh` script.

    * **Inputs:**
        * The query substitution parameters are taken from the directory set in `ldbc.snb.interactive.parameters_dir` configuration property.
        * The update streams are the `updateStream_0_0_{forum,person}.csv` files from the location set in the `ldbc.snb.interactive.updates_dir` configuration property.
        * For this mode, the query frequencies are set to a uniform `1` value to ensure the best average test coverage.
    * **Output:** The results will be stored in the validation parameters file (e.g. `validation_params.csv`) file set in the `create_validation_parameters` configuration property.
    * **Parallelism:** The execution must be single-threaded to ensure a deterministic order of operations.

2. Validate the implementation against an existing reference output (namely the "validation parameters" above) with the `driver/validate.sh` script. This runs a bunch of test queries (one for each type enabled in the properties file).

    * **Input:**
        * The query substitution parameters are taken from the validation parameters file (e.g. `validation_params.csv`) file set in the `validate_database` configuration property.
        * The update operations are also based on the content of the validation parameters file.
    * **Output:**
        * The validation either passes of fails.
        * The per query results of the validation are printed to the console.
        * If the validation failed, the results are saved to the `validation_params-failed-expected.json` and `validation_params-failed-actual.json` files.
    * **Parallelism:** The execution must be single-threaded to ensure a deterministic order of operations.

3. Run the benchmark with the `driver/benchmark.sh` script.

    * **Inputs:**
        * The query substitution parameters are taken from the directory set in `ldbc.snb.interactive.parameters_dir` configuration property.
        * The update streams are the `updateStream_*_{forum,person}.csv` files from the location set in the `ldbc.snb.interactive.updates_dir` configuration property.
            * To get *2n* write threads, the framework requires *n* `updateStream_*_forum.csv` and *n* `updateStream_*_person.csv` files.
            * If you are generating the data sets from scratch, set `ldbc.snb.datagen.serializer.numUpdatePartitions` to *n* in the [data generator](https://github.com/ldbc/ldbc_snb_datagen_hadoop) to get produce these.
        * The goal of the benchmark is the achieve the best (lowest possible) `time_compression_ratio` value while ensuring that the 95% on-time requirement is kept (i.e. 95% of the queries can be started within 1 second of their scheduled time). If your benchmark run returns "failed schedule audit", increase this number (which lowers the time compression rate) until it passes.
        * Set the `thread_count` property to the size of the thread pool for read operations.
        * For audited benchmarks, ensure that the `warmup` and `operation_count` properties are set so that the warmup and benchmark phases last for 30+ minutes and 2+ hours, respectively.
    * **Output:**
        * Passed or failed the "schedule audit" (the 95% on-time requirement).
        * The throughput achieved in the run (operations/second).
        * The detailed results of the benchmark are printed to the console and saved in the `results/` directory.
    * **Parallelism:** Multi-threaded execution is recommended to achieve the best result.

For more details on validating and benchmarking, visit the [driver's documentation](https://github.com/ldbc/ldbc_snb_interactive_v1_driver/tree/main/docs).

## Developer's guide

To create a new implementation, it is recommended to use one of the existing ones: the Neo4j implementation for graph database management systems and the PostgreSQL implementation for RDBMSs.

The implementation process looks roughly as follows:

1. Create a bulk loader which loads the initial data set to the database.
2. Implement the complex and short reads queries (22 in total).
3. Implement the 7 update queries.
4. Test the implementation against the reference implementations using various scale factors.
5. Optimize the implementation.

## Data sets

### Benchmark data sets

To generate the benchmark data sets, use the [Hadoop-based LDBC SNB Datagen](https://github.com/ldbc/ldbc_snb_datagen_hadoop/releases/tag/v1.0.0).

The key configurations are the following:

* `ldbc.snb.datagen.generator.scaleFactor`: set this to `snb.interactive.${SCALE_FACTOR}` where `${SCALE_FACTOR}` is the desired scale factor
* `ldbc.snb.datagen.serializer.numUpdatePartitions`: set this to the number of write threads used in the benchmark runs
* serializers: set these to the required format, e.g. the ones starting with `CsvMergeForeign` or `CsvComposite`
    * `ldbc.snb.datagen.serializer.dynamicActivitySerializer`
    * `ldbc.snb.datagen.serializer.dynamicPersonSerializer`
    * `ldbc.snb.datagen.serializer.staticSerializer`

### Pre-generated data sets

Producing large-scale data sets requires non-trivial amounts of memory and computing resources (e.g. SF100 requires 24GB memory and takes about 4 hours to generate on a single machine).
To mitigate this, we have pregenerated data sets using 9 different serializers and the update streams using 17 different partition numbers:

* Serializers: csv_basic, csv_basic-longdateformatter, csv_composite, csv_composite-longdateformatter, csv_composite_merge_foreign, csv_composite_merge_foreign-longdateformatter, csv_merge_foreign, csv_merge_foreign-longdateformatter, ttl
* Partition numbers: 2^k (1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024) and 6×2^k (24, 48, 96, 192, 384, 768).

The data sets are available at the [SURF/CWI data repository](https://repository.surfsara.nl/datasets/cwi/ldbc-snb-interactive-v1-datagen-v100). We also provide [direct links](https://ldbcouncil.org/data-sets-surf-repository/snb-interactive-v1-datagen-v100) and a [download script](https://ldbcouncil.org/data-sets-surf-repository/#usage) (which stages the data sets from tape storage if they are not immediately available).

### Validation parameters

We provide [**validation parameters for SF0.1 to SF10**](https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/interactive-v1/validation_params-interactive-v1.0.0-sf0.1-to-sf10.tar.zst). These were produced using the Neo4j reference implementation.

### Test data set

Small test data sets are placed in the `cypher/test-data/` directory for Neo4j and in the `postgres/test-data/` for the SQL systems.

To generate a data set with the same characteristics, see the [documentation on generating the test data set](test-data).

## Preparing for an audited run

Implementations of the Interactive workload can be audited by a certified LDBC auditor.
The [Auditing Policies chapter](http://ldbcouncil.org/ldbc_snb_docs/ldbc-snb-specification.pdf#chapter.9) of the specification describes the auditing process and the required artifacts.
If you are considering commissioning an LDBC SNB audit, please study the [auditing process document](https://ldbcouncil.org/docs/ldbc-snb-auditing-process.pdf) and the [audit questionnaire](snb-interactive-audit-questionnaire.md).

### Determining the best TCR

1. Select a scale factor and configure the `driver/benchmark.properties` file as described in the [Driver modes](#driver-modes) section.
2. Load the data set with `scripts/load-in-one-step.sh`.
3. Create a backup with `scripts/backup-database.sh`.
4. Run the `driver/determine-best-tcr.sh`.
5. Once the "best TCR" value has been determined, test it with a full workload (at least 0.5h for warmup operation and at least 2h of benchmark time), and make further adjustments if necessary.

### Recommendations

We have a few recommendations for creating audited implementations. (These are not requirements – implementations are allowed to deviate from these recommendations.)

* The implementation should target a popular Linux distribution (e.g. Ubuntu LTS, CentOS, Fedora).
* Use a containerized setup, where the DBMS is running in a Docker container.
* Instead of a specific hardware, target a cloud virtual machine instance (e.g. AWS `r5d.12xlarge`). Both bare-metal and regular instances can be used for audited runs.