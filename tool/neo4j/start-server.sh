set -eu

sudo nohup neo4j start >&/dev/null &

# wait until ready
set +e
POLL_INTERVAL_SECS=0.5
RETRY_NUM=20
while [[ $RETRY_NUM -gt 0 ]]; do
  lsof -i :7687 > /dev/null
  if [ $? -eq 0 ]; then exit 0; fi
  ((RETRY_NUM-=1))
  sleep $POLL_INTERVAL_SECS
done
set -e
