set -eu

openstack server create "$MACHINE_NAME" \
    --key-name "$OS_KEY_ID" \
    --flavor "$MACHINE_TYPE" \
    --image "$IMAGE" \
    --network Ext-Net

for _ in `seq 60`; do
    STATUS=$(openstack server show "$MACHINE_NAME" |& grep '\bstatus' | awk '{print $4}')
    if [[ $STATUS == "ACTIVE" ]]; then break; fi
done
