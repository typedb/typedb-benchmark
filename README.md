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

## Expected Counts of Data

The databases in this benchmark (presently Grakn and Neo4j) have different data models. The differences in these data models mean that while representing the same information in both systems, the count of modelling entities may be radically different. For example, in Grakn we measure the total size by the `thing` count, which includes `attribute` elements. Whereas in Neo4j we measure `node` and `relation` counts, which do not take into account the `property` count that `node` and `relation` can have.

Breaking this down further, we can see how modelling choices affect the counts.

### Binary Relation
If we choose to represent a binary relation as a `relation` in Grakn and a `relation` in Neo4j we will see that the count of each go up in sync.

### Ternary Relation
If we need to represent a ternary relation, in Grakn we will use a `relation` with three role-players. 

To represent a ternary relation in Neo4j we have to make a choice, we can use:
1. three `relation` elements to connect the three elements pairwise, in our opinion this is a fragile model and should be avoided.
2. one `relation` between the two more major parties of the relation (if they exist) and ignore the third party.
3. one `relation` between the two more major parties of the relation (if they exist) and add the ID of the third party to the `relation`, or otherwise hack in a representation of the third.
4. reify the relation into its own node, and create 3 relations, one to each of the parties involved.

We use cases 2, 3, and 4 in this benchmark. Case 4 in particular lead to a big difference in counts. It means that what would be one relationbecomes a `node` and an additional `N` `relation`s, one for each party involved. This example is demonstrated in the `PurchaseAgent`.

### Nested Relation

To model a relation which is a member of another relation, a _nested_ relation, in Grakn we simply use two relations, with one as a role-player in the other.

We do this, for example, when we want to store the `location` that some relation took place in. In Neo4j we think that most users will not reify their `locates` `relation` into a node jus tto store this information. They are more likely to store some ID or name of the location as a property on the relation itself.

This choice, will mean that we will an increase in the count of `relation` in Grakn. 

In the Neo4j model we will see no increase in Neo4j `node` or `relation` counts since we have added a `property` in model (with the downside that the Neo4j model now has a disconnection in the graph where a users' query has to hop over the ID provided).

### Comparison Test
Overall we can see that the counts on the two DBs do not provide a good measure of the similarity of the data represented by both. To ensure that both models represent the same data we have built a comparison testing suite. This suite runs the benchmark for Grakn and Neo4j iteratively, in parallel, and checks the answers each gives against one another. This gives us the assurance that at all points during the benchmark the exact same information is represented in both systems.