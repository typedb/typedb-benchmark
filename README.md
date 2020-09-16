# Grakn Simulation World

## Build and run Simulation
```shell script
bazel run //:simulation -- -d -k world
```

## Running with Logsplit
Logsplit is a simple Python 3 script designed to split the logs of simulation based on a tracker for ease of testing.

Ensure the `logs/` directory exists.
```shell script
mkdir logs
```
Pipe the output of simulation to logsplit:
```shell script
bazel run //:simulation -- -d grakn -n | python3 logsplit.py
```
The separated logs can then be found in the `logs/` directory.

By running the simulation twice and recording the logs in separate directories, we can look at the diff of the directories to check for determinism. This command is useful:
```shell script
diff -bur logs/ logs1/
```

## CLI options

### Standard Options
```
-d,--database <database>
    Database under test
    REQUIRED

-s,--database-uri <uri>
    Database server URI
    default: dependent upon database

-b,--config-file <config-file-path>
    Absolute path to the YAML config file
    default: config/config_big.yml
```

### Grabl Tracing Options
```
-n,--disable-tracing
    Disable Grabl Tracing

REQUIRED OPTIONS (if tracing is enabled):

-o,--organisation <name>
    Grabl/GitHub organisation for Grabl Tracing

-r,--respository <name>
    Grabl/GitHub repository for Grabl Tracing

-c,--commit <sha>
    Grabl/GitHub commit for Grabl Tracing

-u,--username <username>
    Grabl/GitHub username for Grabl Tracing

-a,--api-token <token>
    Grabl API token for Grabl Tracing
```