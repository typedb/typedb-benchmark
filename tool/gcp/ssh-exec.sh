set -eu

source ./tool/gcp/profile.sh

gcloud compute ssh $MACHINE_NAME --project=$PROJECT --zone=$ZONE --command "$@"