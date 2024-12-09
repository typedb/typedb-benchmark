set -eu

sudo mongod --bind_ip localhost --config /home/factory/typedb-benchmark/tool/mongodb/mongod.conf &
