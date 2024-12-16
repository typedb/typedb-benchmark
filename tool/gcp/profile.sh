set -eu

# cloud provider config
PROJECT=vaticle-engineers
ZONE=europe-west2-c
IMAGE=vaticle-ubuntu-2204-c212752a1d15bc145ca4382452e8a33a354362d6
IMAGE_PROJECT=vaticle-factory-prod

# dev config
# USER=... # your GCP SSH login username
RUN_NUM=1

# machine config
MACHINE_TYPE=n2-standard-16 # m50, m60, m80
MACHINE_TYPE_SHORT="${MACHINE_TYPE:0:2}${MACHINE_TYPE: -2}"
DISK_SIZE=200gb

# TPCC config
DB=typedb3
DB_SHORT="${DB:0:1}${DB: -1}"
SCALE_FACTOR=1
WAREHOUSES=10 # 100, 300, 500, 1000
CLIENTS=1 # 4, 8, 16, 32, 48, 64
DURATION=600

BENCH_ID=b-$USER-db$DB_SHORT-$MACHINE_TYPE_SHORT-$DISK_SIZE-sf$SCALE_FACTOR-w$WAREHOUSES-c$CLIENTS-dur$DURATION-r$RUN_NUM
MACHINE_NAME=$BENCH_ID
