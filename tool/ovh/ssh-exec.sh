set -eu

openstack server ssh $MACHINE_NAME -4 --address-type Ext-Net -- -i $OS_PRIVATE_KEY -l ubuntu -o StrictHostKeychecking=no "$@"
