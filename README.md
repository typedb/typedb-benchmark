# Grakn Simulation World

### Test the CSV-Loader
```shell script
# Load the schema into Grakn
grakn console -k world -f world/schema/schema.gql

# Build the CSV-Loader CLI Tool
bazel build //csv_loader:csv_loader-binary

# Run the tool
csv_loader/csv_loader-binary -k world -f world/data/continent.csv
csv_loader/csv_loader-binary -k world -f world/data/country.csv
```
