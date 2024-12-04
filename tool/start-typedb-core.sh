set -e

# config
BASE_DIR="."
VERSION="82d2e0646f9f5d70f3a8249bd376341dfa4346f6"
DISTRIBUTION="typedb-all-linux-x86_64"
DISTRIBUTION_DIR="$DISTRIBUTION-$VERSION"
DISTRIBUTION_TARGZ="$DISTRIBUTION_DIR.tar.gz"
DISTRIBUTION_URL="https://repo.typedb.com/public/public-snapshot/raw/names/$DISTRIBUTION/versions/$VERSION/$DISTRIBUTION_TARGZ"

cd "$BASE_DIR"
curl -o "$DISTRIBUTION_TARGZ" "$DISTRIBUTION_URL"
tar -xf "$DISTRIBUTION_TARGZ"
./$DISTRIBUTION_DIR/typedb server &

# wait until ready
set +e
POLL_INTERVAL_SECS=0.5
RETRY_NUM=20
while [[ $RETRY_NUM -gt 0 ]]; do
  lsof -i :1729 > /dev/null
  if [ $? -eq 0 ]; then exit 0; fi
  ((RETRY_NUM-=1))
  sleep $POLL_INTERVAL_SECS
done
echo "TypeDB server failed to start within $((POLL_INTERVAL_SECS * RETRY_NUM)) seconds, aborting..."
exit 1
