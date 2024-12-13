set -eu

source ./tool/gcp/profile.sh

gcloud compute scp --project=$PROJECT --zone=$ZONE $USER@$MACHINE_NAME:'~/typedb-benchmark/results.log' result-$MACHINE_NAME.log
