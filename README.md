# Grakn Simulation World

The Simulation World is split into 2 main packages, the `framework` and the `world`. The goal is for the `framework` to provide a library and runtime framework that will collect and run user `Agent`s to perform step-by-step simulation, providing them with the necessary tools to perform database queries and obtain deterministic random numbers in a parallel environment.

The `world` is the user project, that uses the `framework` to create a simulation planet with the goal of creating large amounts of database calls in many use-cases for the Grabl tracing API to capture runtime traces on.

Features of the simulation engine should be added to the `framework` and the `world` will contain all the agent logic (in `world/java`), schemas and data. It can also be responsible for defining the appropriate "testing suites".

NOTE:
Currently `world/java` acts as the runtime, but the plan is to invert it to the `framework` in a future release to keep the `world` logic as clean of engine control as possible.

### Load CSV
```shell script
# Load the schema into Grakn
grakn console -k world -f world/schema/schema.gql

# Build the CSV-Loader CLI Tool
bazel build //csv_loader:csv_loader-binary

# Run the tool
csv_loader/csv_loader-binary -k world -f world/data/continent.csv
csv_loader/csv_loader-binary -k world -f world/data/country.csv
```

### Build and run Simulation

```shell script
bazel build //world/java:world-binary
bazel-bin/world/java/world-binary
```

The expected `stdout` output should contain statements from multiple threads contain a continent name and random number, which should be the same each execution.