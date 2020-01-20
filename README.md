# Grakn Simulation World

### Test the YAML-Tool
```shell script
# Load the schema into Grakn
grakn console -k world -f world/schema/schema.gql

# Build the CSV-Loader CLI Tool
bazel build //yaml_tool:yaml_tool-binary

# Run the tool
bazel-bin/yaml_tool/yaml_tool-binary -k world world/data/data.yaml
```
