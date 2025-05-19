set -euo allexport

usage() {
    echo "Usage: $0 [-d <neo4j|mongodb|postgres|typedb2|typedb3>] [-s SCALE_FACTOR] [-w WAREHOUSES] [-c CLIENTS] [-t DURATION] [-k]" 1>&2
    echo "Default: $0 -d typedb3 -s 1 -w 1 -c 1 -t 600" 1>&2
    echo "-d DATABASE       target database"
    echo "-s SCALE_FACTOR   benchmark scale factor"
    echo "-w WAREHOUSES     number of warehouses"
    echo "-c CLIENTS        number of clients"
    echo "-t DURATION       how long to run the execution portion of the benchmark (seconds)"
    echo "-k                keep the server instance (default: deletes)"
    exit 1
}

KEEP_SERVER=
while getopts ":d:w:c:s:t:k" opt; do
    case $opt in
        d) DB="$OPTARG" ;;
        s) SCALE_FACTOR="$OPTARG" ;;
        w) WAREHOUSES="$OPTARG" ;;
        c) CLIENTS="$OPTARG" ;;
        t) DURATION="$OPTARG" ;;
        k) KEEP_SERVER=1 ;;
        *) usage ;;
    esac
done

source tool/ovh/profile.sh

echo Machine name: $MACHINE_NAME

function cleanup {
    tool/ovh/download-result.sh

    if [[ -z $KEEP_SERVER ]]; then 
        openstack server delete $MACHINE_NAME
    fi
}
trap cleanup ERR
trap cleanup EXIT

tool/ovh/create.sh
sleep 40
tool/ovh/clone-repo.sh $(git rev-parse HEAD)

tool/ovh/ssh-exec.sh "'cd typedb-benchmark && tool/$DB/setup.sh'"
tool/ovh/ssh-exec.sh "'cd typedb-benchmark && tool/postgres/setup.sh'"

tool/ovh/ssh-exec.sh "'
    cd typedb-benchmark && . venv/bin/activate &&
        nohup tool/execute-tpcc.sh --no-execute --reset --scalefactor=$SCALE_FACTOR --warehouses=$WAREHOUSES --clients=$CLIENTS --duration=$DURATION --verify $DB & wait \$!
    '"

tool/ovh/ssh-exec.sh "'
    cd typedb-benchmark && . venv/bin/activate &&
        nohup tool/execute-tpcc.sh --no-load --scalefactor=$SCALE_FACTOR --warehouses=$WAREHOUSES --clients=$CLIENTS --duration=$DURATION --verify $DB & wait \$!
    '"
