set -eu

echo 'installing driver...'
sudo apt update -y
sudo apt install -y python3-pip
python3 -m pip install pymongo
echo 'installing driver done'