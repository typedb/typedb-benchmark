set -eu

toool/ovh/ssh-exec.sh "tail -n +1 -F typedb-benchmark/results.log"
