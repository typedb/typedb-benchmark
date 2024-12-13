set -eu

source ./tool/gcp/profile.sh

gcloud compute instances delete $MACHINE_NAME --project=$PROJECT --zone=$ZONE