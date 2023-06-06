# TypeDB read-write benchmark
This benchmark contains a 'minimal set of representative queries' to emulate a typical typedb workload.
It was developed to evaluate the effects of replacing RocksDB with SpeeDB on read & write performance.

### Requirements 
* Bazel v5.1.1
* A running [typedb server](https://github.com/vaticle/typedb) to benchmark against.

### Running
The easiest way to run the benchmark is through bazel:

```bash
bazel run //read-write:run -- --database=typedb --address=127.0.0.1:1729 --config=read-write/config/<config-file>.yml
```

The <config-file> determines which queries will be run.
* `ci-tests.yml`: Runs a few iterations to ensure everything is working properly (< 1 GB)
* `write-benchmark.yml`: Only performs the write queries (~10 GB)
* `read-write-benchmark.yml`: Performs (all) read & write queries described below (~10 GB)
* `large-data-benchmark.yml`: Performs all queries except `readAddressFromPostCode` for a lage number of iterations (~350 GB)

### Benchmark

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
    