set -eu

source ./tool/gcp/profile.sh

# run the command in a tmux session, and add a sleep because otherwise it will exit immediately without executing the benchmark
tool/gcp/ssh-exec.sh "tmux new-session -d -s 'benchmark' 'cd typedb-benchmark && tool/execute-tpcc.sh --no-load --scalefactor=$SCALE_FACTOR --warehouses=$WAREHOUSES --clients=$CLIENTS --duration=$DURATION $DB; sleep 10'"