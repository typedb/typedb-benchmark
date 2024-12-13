set -eu

source tool/typedb2/config.sh

# start server
DISTRIBUTION="typedb-all-linux-x86_64"
DISTRIBUTION_DIR="$DISTRIBUTION-$SERVER_VERSION"
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
