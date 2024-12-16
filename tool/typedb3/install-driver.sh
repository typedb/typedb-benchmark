set -eu

source tool/typedb3/config.sh

sudo apt update -y
sudo apt install -y python3-pip
pip install typedb-driver=="$DRIVER_VERSION" --index-url https://repo.typedb.com/public/public-snapshot/python/simple/