set -eu

tool/postgres/install-driver.sh
tool/postgres/install-server.sh
tool/postgres/configure-server.sh
tool/postgres/start-server.sh
