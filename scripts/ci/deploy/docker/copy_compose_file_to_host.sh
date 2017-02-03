#!/usr/bin/env bash

# Exit  immediately  if  a pipeline (which may consist of a single simple command), a list, or a compound command
# (see SHELL GRAMMAR above),  exits with a non-zero status.
set -e
# After expanding each simple command, for command, case command, select command, or arithmetic for command,
# display the expanded value of PS4, followed by the command and its expanded  arguments  or  associated word list.
#set -x

COMPOSE_BIN_FOLDER="~/osm"
stderr_file="osm_${HOST_DOCKER}_stderr.txt"
echo "stderr_file=${stderr_file}"

echo "create directory for osm on target host"
echo "####################################################"
echo "-> create directory $COMPOSE_BIN_FOLDER"
ssh -p $SSH_PORT_TO_HOST_DOCKER -o "StrictHostKeyChecking no" $OSM_HOSTS_OS_USER@$HOST_DOCKER /bin/bash <<EOT
echo "" > /tmp/${stderr_file}
if [ ! -d $COMPOSE_BIN_FOLDER ]; then
	mkdir $COMPOSE_BIN_FOLDER
fi 2> /tmp/${stderr_file}
EOT

echo "get stderr from remote host"
scp -P $SSH_PORT_TO_HOST_DOCKER -o "StrictHostKeyChecking no" $OSM_HOSTS_OS_USER@$HOST_DOCKER:"/tmp/$stderr_file" .
echo "-> stderr create folder START"
cat "./$stderr_file"
echo "-> stderr create folder END"

echo "copy compose file to target host"
echo "####################################################"
scp -P $SSH_PORT_TO_HOST_DOCKER -o "StrictHostKeyChecking no" ./docker/docker-compose-behind-reverse-proxy.yml $OSM_HOSTS_OS_USER@$HOST_DOCKER:"${COMPOSE_BIN_FOLDER}/docker-compose.yml"