# Simple Grakn CSV-Loader

This library can be used to load a list of entities with simple 1-to-1 relations from a CSV into the grakn server. It can also operate as a command line tool.

## CLI

Usage:

    csv_loader-binary -f <file> -k <keyspace>
    
Additionally, input can be piped to `stdin`, or the grakn server URI can be specified with `-u <hostname>:<port>`.

## Library

Include the `csv_loader` Java library in your dependencies.

Tutorial:
```java
class Sample {
    public static void main(String[] args) {
        GraknClient client = new GraknClient();
        GraknClient.Session session = client.session("my_keyspace");
        File file = new File("my_entity_type.csv");
        
        new GraknCSVLoader(session).loadEntity("my_entity_type", file);
    }
}
```

## CSV Format

Each CSV file represents a list of entities of a single type. When loading the file with the command line tool, the name of the file (excluding the extension) is used as the entity type name.

The CSV headers represent how the data of the column should be used

### Attribute Column

A column with a header not starting with a `:` is treated as an attribute of the entity. The column header dictates the attribute name, whilst the column values represent the actual attributes that the entity will have.

Example file (`person.csv`):
```csv
first-name,last-name
John,Smith
Haikal,Pribadi
```

Would generate and execute a Graql insert statement equivalent to:
```
insert
  $__1 isa person, has first-name John, has last-name Smith;
  $__2 isa person, has first-name Haikal, has last-name Pribadi;
```

### Relation Column

A column with a header starting with a `:` is a relation column that specifies a relation in the format:

    :<relation-type>:<new-entity-role>:<existing-entity-role>:<existing-entity-type>:<existing-entity-attribute-name>

The column value is the value of the attribute in the header used to match the existing entity to relate this new entity to.

Example file (country.csv):
```csv
country-name,:contains:containee:container:continent:continent-name
United Kingdom,Europe
USA,North America
Germany,Europe
```

Would generate and execute a Graql match-insert statement equivalent to:
```
match
  $continent__Europe isa continent, has continent-name "Europe";
  $continent__North_America isa continent, has continent-name "North America";
insert
  $__1 isa country, has country-name "United Kingdom";
  $_ (container: $continent__Europe, containee: $__1) isa container;
  $__2 isa country, has country-name "USA";
  $_ (container: $continent__North_America, containee: $__2) isa container;
  $__3 isa country, has country-name "Germany";
  $_ (container: $continent__Europe, containee: $__3) isa container;
```


