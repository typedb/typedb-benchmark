set -eu

source ./tool/gcp/profile.sh

gcloud compute instances create $MACHINE_NAME \
    --project=$PROJECT \
    --zone=$ZONE \
    --machine-type=$MACHINE_TYPE \
    --image=$IMAGE \
    --image-project=$IMAGE_PROJECT \
    --boot-disk-size=$DISK_SIZE