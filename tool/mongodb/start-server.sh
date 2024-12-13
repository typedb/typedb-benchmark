set -eu

sudo mongod --replSet rs0 --bind_ip localhost --config ./tool/mongodb/mongod.conf &
