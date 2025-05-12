set -eu

# TPCC config
DB=${DB:-typedb3}
source tool/$DB/config.sh # export the DB server and driver version
SCALE_FACTOR=${SCALE_FACTOR:-1} # 0.5, 1
WAREHOUSES=${WAREHOUSES:-1} # 1, 5, 10, --- 100, 300, 500, 1000
CLIENTS=${CLIENTS:-1} # 4, 8, 16, 32, 48, 64
DURATION=${DURATION:-600}

# machine config
MACHINE_TYPE=b2-15

# cloud provider config
if [ ! -v OS_TENANT_ID -o ! -v OS_TENANT_NAME ]; then
    echo "OS_TENANT_ID and OS_TENANT_NAME must be set; download and source openrc.sh"
    exit 1
fi

if [ ! -v OS_USERNAME -o ! -v OS_PASSWORD ]; then
    echo "OS_USERNAME and OS_PASSWORD must be set; download and source openrc.sh"
    exit 1
fi

if [ ! -v OS_KEY_ID -o ! -v OS_PRIVATE_KEY ]; then
    echo "OS_KEY_ID and OS_PRIVATE_KEY must be set"
    exit 1
fi

PROJECT=vaticle-engineers
ZONE=europe-west2-c
IMAGE="Ubuntu 24.10"
IMAGE_PROJECT=vaticle-factory-prod

# four digit random number with zero padding
ID=0000$RANDOM
ID=${ID:(-4)}

# extrapolation
DB_SHORT="${DB:0:1}${DB: -1}"
SERVER_VERSION_SHORT=${SERVER_VERSION:0:4}
SERVER_VERSION_SHORT=${SERVER_VERSION_SHORT//\./-} # replace '.'s in version with hyphens
DRIVER_VERSION_SHORT="eac3" # TODO: update according to DRIVER_VERSION
BENCH_ID=$USER-$DB_SHORT-$SERVER_VERSION_SHORT-$DRIVER_VERSION_SHORT-$MACHINE_TYPE-sf$SCALE_FACTOR-w$WAREHOUSES-c$CLIENTS-dur$DURATION-$ID
MACHINE_NAME=$BENCH_ID
