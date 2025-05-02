set -eu

openstack server create $MACHINE_NAME \
    --key-name $OS_KEY_ID \
    --flavor $MACHINE_TYPE \
    --image $IMAGE \
    --network Ext-Net
