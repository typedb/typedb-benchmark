set -eu

source tool/mongodb/config.sh

echo 'installing server...'
sudo apt-get install -y gnupg curl
curl -fsSL https://www.mongodb.org/static/pgp/server-8.0.asc | sudo gpg -o /usr/share/keyrings/mongodb-server-8.0.gpg --dearmor
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-8.0.gpg ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/8.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-8.0.list
sudo apt-get update -y
sudo apt-get install -y mongodb-org=$SERVER_VERSION mongodb-org-database=$SERVER_VERSION mongodb-org-server=$SERVER_VERSION mongodb-mongosh mongodb-org-mongos=$SERVER_VERSION mongodb-org-tools=$SERVER_VERSION
ulimit -f unlimited - t unlimited -v unlimited -l unlimited -n 64000 -m unlimited -u 64000
echo 'installing server done'