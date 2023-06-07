## TypeDB read-write benchmark
This benchmark contains a 'minimal set of representative queries' to emulate a typical typedb workload.
It was developed to evaluate the effects of replacing RocksDB with SpeeDB on read & write performance,
and to evaluate the effect of any future changes on performance.
### Requirements 
* Bazel v5.1.1
* A running [typedb server](https://github.com/vaticle/typedb) v2.16.1 to benchmark against.

### Running
The easiest way to run the benchmark is through bazel:

```bash
bazel run //read-write:benchmark-runner -- --database=typedb --address=127.0.0.1:1729 --config=read-write/config/<config-file>.yml
```

Replace the address if you're not running the server on the same machine or using TypeDB's default port. 

The <config-file> determines which queries will be run.
* `ci-tests.yml`: Runs a few iterations to ensure everything is working properly (< 1 GB)
* `read-write-benchmark.yml`: Performs (all) read & write queries described below (~10 GB)
* `write-benchmark.yml`: Only performs write queries described below (~350 GB)
* `large-data-benchmark.yml`: Performs all queries except `readAddressFromPostCode` for a large number of iterations (~350 GB)

### Benchmark Queries

The `PersonAgent` currently implements 6 queries.
* `createPerson`:
  * Inserts a `person` entity with 3 attributes - `name`, `address`, and `postCode`.
* `createFriendship`:
  * selects two names at random (on the client-side)
  * runs a 'match-insert' query to create a `friendship` relation between them, with a `meeting-time` attribute attached.
* `readAddressFromName`: 
  * selects a name at random (client-side)
  * finds the person with the name
  * then finds their address. 
* `readFriendsOf`: pure-read
  * selects a name at random (client-side)
  * finds the person with the name
  * then finds all friendships and the friend (other person) in each friendship
* `readFriendsOfFriends`: pure-read
  * selects a name at random (client-side)
  * finds the person with the name
  * then finds __all__ friendships and the friend in each friendship
  * for each such 'other person', it finds __all__ their friendship relations and the associated friends. 
* `readPersonsByPostCode`: pure-read
  * selects a postCode at random (client-side)
  * finds __all__ persons with the associated postCode.
  * __Note__: Since the number of postCodes is fixed, this query scales linearly as more persons are inserted

### Storage perspective
This section describes the operations at the storage level for each of the queries above.
Traversals are performed depth-first. The number of results described is for the `large-data-benchmark.yml` config.

* `createPerson`: pure writes
    * inserts 10 keys: 1 entity + 3 attributes + 3 edges * 2 keys/edge 
* `createFriendship`: interleaves reads & writes 
  * 2 point lookups + 2 seeks: The seek will only have 1 result (unless hash collisions occur)
  * inserts 8 keys: 1 relation + 1 attribute + 3 edges * 2 keys/edge.
* `readAddressFromName`: Point lookups
  * Does a point lookup for the name
  * Iterates over the edges to the persons with the name (1 result)
  * Iterates over the edges to the addresses of the person (1 result)
* `readFriendsOf`: iterates over a set of keys.
  * Does a point lookup for the name
  *  Iterates over the edges to the persons with the name (1 result)
  *  Iterates over the edges to each friendship relation of the person (~5 results).
  * For each relation:
    *  Iterates over the edges to the associated friend (1 result)
* `readFriendsOfFriends`: nested iteration
  * Does a point lookup for the name
  * Iterates over the edges to the persons with the name (1 result)
  * Iterates over the edges to each friendship relation of the person (~5 results).
  * For each relation:
    * Iterates over the edges to the associated friend (1 result)
    * For each friend:
      * Iterates over the edges to each of _their_ friendship relations (~5 results).
      * For each relation:
        * Iterates over the edges to the associated friend (1 result)

* `readPersonsByPostCode`: iterates over a large number of keys.
  * Does a point lookup for the postCode
  * Iterates over the edges to every person entity having the postCode (nPersons/nPostCodes on average)

### Benchmark architecture
A benchmark is required to define a set of agents, each of which defines a set of actions it can perform.
A configuration file determines which actions are run.

The benchmark will repeat the set of actions for a number of iterations defined by the `run.Iterations` parameter.
Actions are run serially, reflecting the order in which they appear under `agents`.

For each action, the framework will create `run.nPartition` instances of the corresponding agent. Each agent is to execute the task `agent.<action>.runsPerIteration` times.
The framework adds all the instances to a thread pool, the size of which is determined by the `run.parallelism` parameter.
Once all agents have performed the action `runsPerIteration` times, the framework reports the time taken.

#### Parameters:
* **agents** : Contains a list of tasks to be run
  - name: The name of the agent, which is mapped to a Kotlin class in the implementation
  - action: The name of the task, which is mapped to a function within the agent class.
  - runsPerIteration: The number of times per iteration (and per partition) this task will be performed.
  - trace: Enable factory tracing (internal) 

* **run**: Parameters for the benchmark framework
  - randomSeed: The seed for the random number generator. (Each partition has an isolated RNG)
  - iterations: The total number of iterations to run the benchmark for. 
  - partitions: The number of partitions (= logical copies of each agent)
  - databaseName: The name of the database to create/use in typedb
  - recreateDatabase: if true, deletes and re-creates the typedb database.
  - parallelism: The size of the threadpool on which the partitions are executed

* **model**: Benchmark specific settings which are passed on to the agents
  - personsCreatedPerRun: The number of persons inserted in each `CreatePerson` action
  - friendshipsCreatedPerRun: The number of friendships inserted in each `CreatePerson` action
  - postCodes: The number of `postCodes` which exist.
