set -eu

source ./tool/gcp/profile.sh

tool/gcp/create.sh
sleep 40
tool/gcp/clone-repo.sh $@

# run in the background as the TypeDB process will block the execution otherwise
tool/gcp/ssh-exec.sh "cd typedb-benchmark && tool/$DB/setup.sh" &
