set -eu

source ./tool/gcp/profile.sh

gcloud compute scp --project=$PROJECT --zone=$ZONE $USER@$MACHINE_NAME:'~/typedb-benchmark/results.log' result-$MACHINE_NAME.log
gcloud compute scp --project=$PROJECT --zone=$ZONE $USER@$MACHINE_NAME:"~/typedb-benchmark/typedb-all-linux-x86_64-$SERVER_VERSION/log" result-$MACHINE_NAME-server.log
