set -e

# config
BASE_DIR="."
VERSION="3.0.0a7"

sudo apt update -y
sudo apt install -y python3-pip
pip install typedb-driver=="$VERSION"
