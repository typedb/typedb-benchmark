# Grakn Simulation World

### Build and run Simulation
```shell script
bazel run //:simulation-binary
```

The expected `stdout` output should contain statements from multiple threads contain a continent name and random number, which should be the same each execution.