# cloud provider config
PROJECT=vaticle-engineers
ZONE=europe-west2-c
IMAGE=vaticle-ubuntu-2204-c212752a1d15bc145ca4382452e8a33a354362d6
IMAGE_PROJECT=vaticle-factory-prod

# dev config
USER=lolski
RUN_NUM=7

# machine config
MACHINE_TYPE=n2-standard-16
DISK_SIZE=50gb

# TPCC config
DB=mongodb
SCALE_FACTOR=1
WAREHOUSES=1
CLIENTS=1
DURATION=1

BENCH_ID=b-$USER-$MACHINE_TYPE-$DISK_SIZE-sf$SCALE_FACTOR-w$WAREHOUSES-c$CLIENTS-dur$DURATION-db$DB-r$RUN_NUM
MACHINE_NAME=$BENCH_ID
