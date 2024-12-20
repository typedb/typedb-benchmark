set -eu

source ./tool/gcp/profile.sh

gcloud compute scp --project=$PROJECT --zone=$ZONE $USER@$MACHINE_NAME:'~/typedb-benchmark/results.log' result-$MACHINE_NAME.log

# resolve typedb folder using glob '*', as there's exactly one typedb folder
gcloud compute scp --project=$PROJECT --zone=$ZONE $USER@$MACHINE_NAME:"~/typedb-benchmark/typedb-all-linux-x86_64-*/log" result-$MACHINE_NAME-server.log
