set -eu

tool/mongodb/install-server.sh
tool/mongodb/install-driver.sh
tool/mongodb/start-server.sh
tool/mongodb/init-replica-set.sh
