set -eu

openstack server ssh $MACHINE_NAME --address-type Ext-Net -- -i $OS_PRIVATE_KEY -l ubuntu
