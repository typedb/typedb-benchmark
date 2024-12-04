set -e

# config
BASE_DIR="."
ARCH="x86_64"
VERSION="82d2e0646f9f5d70f3a8249bd376341dfa4346f6"
DISTRIBUTION_DIR="typedb-all-mac-$ARCH-$VERSION"
DISTRIBUTION_ZIP="$DISTRIBUTION_DIR.zip"
DISTRIBUTION_URL="https://repo.typedb.com/public/public-snapshot/raw/names/typedb-all-mac-$ARCH/versions/$VERSION/$DISTRIBUTION_ZIP"

cd "$BASE_DIR"
curl -o "$DISTRIBUTION_ZIP" "$DISTRIBUTION_URL"
unzip "$DISTRIBUTION_ZIP"
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
