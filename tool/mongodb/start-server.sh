set -eu

sudo mongod --replSet rs0 --bind_ip localhost --config /home/factory/typedb-benchmark/tool/mongodb/mongod.conf &
