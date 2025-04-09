set -euo allexport

usage() {
    echo "Usage: $0 [-d <neo4j|mongodb|postgres|typedb2|typedb3>] [-s SCALE_FACTOR] [-w WAREHOUSES] [-c CLIENTS] [-t DURATION]" 1>&2
    echo "Default: $0 -d typedb3 -s 1 -w 1 -c 1 -t 600" 1>&2
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

source tool/gcp/profile.sh

echo Machine name: $MACHINE_NAME

tool/gcp/create.sh
sleep 40
tool/gcp/clone-repo.sh $(git rev-parse HEAD)

# run in the background as the TypeDB process will block the execution otherwise
tool/gcp/ssh-exec.sh "cd typedb-benchmark && tool/$DB/setup.sh"

tool/gcp/ssh-exec.sh "
    cd typedb-benchmark && 
        nohup tool/execute-tpcc.sh --no-execute --reset --scalefactor=$SCALE_FACTOR --warehouses=$WAREHOUSES --clients=$CLIENTS --duration=$DURATION $DB >/dev/null & wait \$!
    "

tool/gcp/ssh-exec.sh "
    cd typedb-benchmark && 
        nohup tool/execute-tpcc.sh --no-load --scalefactor=$SCALE_FACTOR --warehouses=$WAREHOUSES --clients=$CLIENTS --duration=$DURATION $DB >/dev/null & wait \$!
    "

tool/gcp/download-result.sh

if [[ -z $KEEP_SERVER ]]; then 
    gcloud compute instances delete $MACHINE_NAME --project=$PROJECT --zone=$ZONE --quiet
fi
