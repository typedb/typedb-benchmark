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
DISK_SIZE=200gb

# TPCC config
DB=mongodb
SCALE_FACTOR=1
WAREHOUSES=100 # 100, 300, 500, 1000
CLIENTS=8 # 4, 8, 16, 32, 48, 64
DURATION=1800

BENCH_ID=b-$USER-$MACHINE_TYPE-$DISK_SIZE-sf$SCALE_FACTOR-w$WAREHOUSES-c$CLIENTS-dur$DURATION-db$DB-r$RUN_NUM
MACHINE_NAME=$BENCH_ID
