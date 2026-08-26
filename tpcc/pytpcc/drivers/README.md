# PyTPCC for TypeDB

Requires TypeDB server 3.12.0+ and python `typedb-driver` 3.12.0+ (the `typedb3` driver uses the TypeQL `given` clause with `given_rows` query parameters) — see `tpcc/README.md` for details.

## Run the benchmark

### Data load

```
python tpcc.py --scalefactor=10000 --warehouses=1 --no-execute --debug --reset --clients=1 typedb3
```

### Query benchmark

...

