# Grakn Benchmarkng System: A World Simulation

## Build and run Benchmark
```shell script
bazel run //:benchmark -- -d grakn -n
```

## Running with Logsplit
Logsplit is a simple Python 3 script designed to split the logs of benchmark based on a tracker for ease of testing.

Ensure the `logs/` directory exists.
```shell script
mkdir logs
```
Pipe the output of benchmark to logsplit:
```shell script
bazel run //:benchmark-big -- -d grakn -n | python3 logsplit.py
```
The separated logs can then be found in the `logs/` directory.

By running the benchmark twice and recording the logs in separate directories, we can look at the diff of the directories to check for determinism. This command is useful:
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

## Generating a Benchmark Report

Navigate to the `benchmark` package

Requirements:
- Python 3.6+
- `pip install requirements.txt`

Usage:
```
# Make an environment variable that the Python script can use to access the Grabl endpoint for benchmark chart data 
export GRABL_USER_TOKEN="<your_grabl_user_token>"

# Fetch chart data from a Grabl endpoint, create charts using matplotlib and save them to disk
python3 ../plots/report_plots.py <commit_sha> <grakn_analysis_id> <neo4j_analysis_id> <overview_iterations_to_plot>

# Generate a .tex file of the report based on .tex files as templates, data extracted from the benchmark code (queries and
 agent names) and the charts created via the Python script. This is a Kotlin class.
bazel run //benchmark:benchmark

# From the generated .tex file build a pdf
pdflatex -interaction=nonstopmode report.tex

# Open the pdf (mac)
open report.pdf
```

For example, you can try this as a bash script:
```
export GRABL_USER_TOKEN="8h0r403h1f0f743" &&
python3 ../plots/report_plots.py c63e451b8d8a3c56d8466446212987af7904f7e4 7494121211537405952 8288812439696326656 4 8 12 &&
bazel run //benchmark:benchmark &&
pdflatex -interaction=nonstopmode report.tex &&
open report.pdf
```

Note that `4 8 12` are the iterations that will be plotted on separate axes in the overview chart. You can give as many iterations as you like.
See `python3 report_plots.py --help` for help with this command.
