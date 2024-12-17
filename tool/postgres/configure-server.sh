set -eu

tool/postgres/start-server.sh
sudo su postgres -c "psql -c 'CREATE USER factory SUPERUSER;'"
createdb
sudo cp tool/postgres/conf/postgresql.conf /etc/postgresql/17/main/postgresql.conf
sudo cp tool/postgres/conf/pg_hba.conf /etc/postgresql/17/main/pg_hba.conf
psql -c "SELECT pg_reload_conf()"
tool/postgres/stop-server.sh
