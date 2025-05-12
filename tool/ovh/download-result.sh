openstack server ssh $MACHINE_NAME -4 --address-type Ext-Net -- -i $OS_PRIVATE_KEY -l ubuntu -o StrictHostKeychecking=no "cat ~/typedb-benchmark/results.log" > result-$MACHINE_NAME.log
# resolve typedb folder using glob '*', as there's exactly one typedb folder
if [[ $DB =~ typedb ]]; then
    openstack server ssh $MACHINE_NAME -4 --address-type Ext-Net -- -i $OS_PRIVATE_KEY -l ubuntu -o StrictHostKeychecking=no "cat ~/typedb-benchmark/typedb-all-linux-x86_64-*/log" > result-$MACHINE_NAME-server.log
fi

