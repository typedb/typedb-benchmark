set -e

kill $(ps aux | awk '/typedb[_server_bin]/ {print $2}')
