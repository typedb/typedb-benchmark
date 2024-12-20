set -eu

# TPCC config
DB=typedb3
source tool/$DB/config.sh # export the DB server and driver version
SCALE_FACTOR=1 # 0.5, 1
WAREHOUSES=1 # 1, 5, 10, --- 100, 300, 500, 1000
CLIENTS=1 # 4, 8, 16, 32, 48, 64
DURATION=600

# machine config
MACHINE_TYPE=n2-standard-16 # m50, m60, m80
DISK_SIZE=200gb

# run config
# USER=... # your GCP SSH login username
RUN_NUM=2

# cloud provider config
PROJECT=vaticle-engineers
ZONE=europe-west2-c
IMAGE=vaticle-ubuntu-2204-c212752a1d15bc145ca4382452e8a33a354362d6
IMAGE_PROJECT=vaticle-factory-prod

# extrapolation
DB_SHORT="${DB:0:1}${DB: -1}"
SERVER_VERSION_SHORT=${SERVER_VERSION:0:4}
DRIVER_VERSION_SHORT="eac3" # TODO: update according to DRIVER_VERSION
MACHINE_TYPE_SHORT="${MACHINE_TYPE:0:2}${MACHINE_TYPE: -2}"
BENCH_ID=b-$USER-db$DB_SHORT-$SERVER_VERSION_SHORT-$DRIVER_VERSION_SHORT-$MACHINE_TYPE_SHORT-$DISK_SIZE-sf$SCALE_FACTOR-w$WAREHOUSES-c$CLIENTS-dur$DURATION-r$RUN_NUM
MACHINE_NAME=$BENCH_ID
