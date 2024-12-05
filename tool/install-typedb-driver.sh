set -eu

source tool/config.sh

sudo apt update -y
sudo apt install -y python3-pip
pip install typedb-driver=="$DRIVER_VERSION"
