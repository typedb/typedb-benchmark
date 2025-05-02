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
OS_AUTH_URL=https://auth.cloud.ovh.net/v3
OS_IDENTITY_API_VERSION=3

OS_USER_DOMAIN_NAME=${OS_USER_DOMAIN_NAME:-"Default"}
OS_PROJECT_DOMAIN_NAME=${OS_PROJECT_DOMAIN_NAME:-"Default"}

if [ ! -v OS_TENANT_ID -o ! -v OS_TENANT_NAME -o ! -v OS_USERNAME -o ! -v OS_PASSWORD -o ! -v OS_KEY_ID -o ! -v OS_PRIVATE_KEY]; then
    echo "All of OS_TENANT_ID, OS_TENANT_NAME, OS_USERNAME, OS_PASSWORD must be set"
    exit 1
fi

OS_REGION_NAME="UK1"

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
MACHINE_TYPE_SHORT="${MACHINE_TYPE:0:2}${MACHINE_TYPE: -2}"
BENCH_ID=$USER-$DB_SHORT-$SERVER_VERSION_SHORT-$DRIVER_VERSION_SHORT-$MACHINE_TYPE_SHORT-$DISK_SIZE-sf$SCALE_FACTOR-w$WAREHOUSES-c$CLIENTS-dur$DURATION-$ID
MACHINE_NAME=$BENCH_ID
