set -eu

source tool/typedb2/config.sh

sudo apt update -y
sudo apt install -y python3-pip python3-venv
if [ ! -x venv ]; then python3 -m venv venv; fi
. venv/bin/activate
pip install typedb-driver=="$DRIVER_VERSION"
