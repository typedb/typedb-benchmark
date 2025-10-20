set -eu

# TPCC config
DB=${DB:-typedb3}
source tool/$DB/config.sh # export the DB server and driver version
SCALE_FACTOR=${SCALE_FACTOR:-1} # 0.5, 1
WAREHOUSES=${WAREHOUSES:-1} # 1, 5, 10, --- 100, 300, 500, 1000
CLIENTS=${CLIENTS:-1} # 4, 8, 16, 32, 48, 64
DURATION=${DURATION:-600}

# machine config
MACHINE_TYPE=n2-standard-16 # m50, m60, m80
DISK_SIZE=200gb

# run config
# USER=... # your GCP SSH login username

# cloud provider config
PROJECT=typedb-engineers
ZONE=europe-west2-c
IMAGE=vaticle-ubuntu-2204-c212752a1d15bc145ca4382452e8a33a354362d6
IMAGE_PROJECT=vaticle-factory-prod

# four digit random number with zero padding
ID=0000$RANDOM
ID=${ID:(-4)}

# extrapolation
DB_SHORT="${DB:0:1}${DB: -1}"
SERVER_VERSION_SHORT=${SERVER_VERSION:0:4}
SERVER_VERSION_SHORT=${SERVER_VERSION_SHORT//\./-} # replace '.'s in version with hyphens
DRIVER_VERSION_SHORT="eac3" # TODO: update according to DRIVER_VERSION
MACHINE_TYPE_SHORT="${MACHINE_TYPE:0:2}${MACHINE_TYPE: -2}"
BENCH_ID=$USER-$DB_SHORT-$SERVER_VERSION_SHORT-$DRIVER_VERSION_SHORT-$MACHINE_TYPE_SHORT-$DISK_SIZE-sf$SCALE_FACTOR-w$WAREHOUSES-c$CLIENTS-dur$DURATION-$ID
MACHINE_NAME=$BENCH_ID
