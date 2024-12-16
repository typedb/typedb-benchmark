set -eu

VERSION_VALUE=$1
CONFIG_FILE="tool/typedb3/config.sh"
sed -i'' -E "s/^(SERVER_VERSION=).*/\1$VERSION_VALUE/" $CONFIG_FILE
