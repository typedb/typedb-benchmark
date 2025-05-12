set -eu

tool/ovh/ssh-exec.sh "'git clone https://github.com/typedb/typedb-benchmark.git'"
if [ $# -eq 1 ]; then
  REF=$1
  tool/ovh/ssh-exec.sh "'cd typedb-benchmark && git fetch origin $REF && git checkout $REF'"
fi
