set +e
set -u
sudo kill $(ps aux | awk '/mongod/ {print $2}') || true
set -e