set -eu

source tool/neo4j/config.sh

sudo add-apt-repository -y ppa:openjdk-r/ppa

sudo mkdir -p /etc/apt/keyrings
wget -O - https://debian.neo4j.com/neotechnology.gpg.key | sudo gpg --dearmor -o /etc/apt/keyrings/neotechnology.gpg
echo 'deb [signed-by=/etc/apt/keyrings/neotechnology.gpg] https://debian.neo4j.com stable 5' | sudo tee -a /etc/apt/sources.list.d/neo4j.list
sudo apt update -y
sudo apt install -y neo4j=1:$SERVER_VERSION

sudo update-java-alternatives --jre --set java-1.21.0-openjdk-amd64
sudo sed -i 's/#dbms.security.auth_enabled=.*/dbms.security.auth_enabled=false/' /etc/neo4j/neo4j.conf
