set -eu

source tool/typedb3/config.sh

sudo apt update -y
sudo apt install -y python3-pip python3-venv
if [ ! -x venv ]; then python3 -m venv venv; fi
. venv/bin/activate
pip install typedb-driver=="$DRIVER_VERSION" --extra-index-url https://repo.typedb.com/public/public-snapshot/python/simple/
