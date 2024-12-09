set -eu

sudo kill $(ps aux | awk '/mongod/ {print $2}') || true