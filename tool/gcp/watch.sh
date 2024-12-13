set -eu

tool/gcp/ssh-exec.sh "tail -n +1 -F typedb-benchmark/results.log"