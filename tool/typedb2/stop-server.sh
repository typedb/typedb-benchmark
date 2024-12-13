set -eu

kill $(ps aux | awk '/TypeDBServer/ {print $2}')
