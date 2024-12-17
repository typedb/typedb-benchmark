set -eu

echo 'installing driver...'
sudo apt update -y
sudo apt install -y python3-pip
pip install psycopg2-binary
echo 'installing driver done'
