set -eu

source tool/typedb3/config.sh

DISTRIBUTION="typedb-all-linux-x86_64"
DISTRIBUTION_DIR="$DISTRIBUTION-$SERVER_VERSION"
DISTRIBUTION_TARGZ="$DISTRIBUTION_DIR.tar.gz"
DISTRIBUTION_URL="https://repo.typedb.com/public/public-snapshot/raw/names/$DISTRIBUTION/versions/$SERVER_VERSION/$DISTRIBUTION_TARGZ"

curl -o "$DISTRIBUTION_TARGZ" "$DISTRIBUTION_URL"
tar -xf "$DISTRIBUTION_TARGZ"
