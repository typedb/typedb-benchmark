set -e

source tool/config.sh

BASE_DIR="."
DISTRIBUTION="typedb-all-linux-x86_64"
DISTRIBUTION_DIR="$DISTRIBUTION-$SERVER_VERSION"
DISTRIBUTION_TARGZ="$DISTRIBUTION_DIR.tar.gz"

kill $(ps aux | awk '/typedb[_server_bin]/ {print $2}')

rm -rf "$DISTRIBUTION_DIR"
rm "$DISTRIBUTION_DISTRIBUTION_TARGZ"