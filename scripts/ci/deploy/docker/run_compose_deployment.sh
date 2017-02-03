#!/usr/bin/env bash

# Exit  immediately  if  a pipeline (which may consist of a single simple command), a list, or a compound command
# (see SHELL GRAMMAR above),  exits with a non-zero status.
set -e
# After expanding each simple command, for command, case command, select command, or arithmetic for command,
# display the expanded value of PS4, followed by the command and its expanded  arguments  or  associated word list.
#set -x

COMPOSE_BIN_FOLDER="~/OsmDetailAnalysis"
stderr_file="osm_${osm_host}_stderr.txt"
echo "stderr_file=${stderr_file}"

echo "run compose deployment on target host"
echo "####################################################"
ssh -p $osm_host_ssh_port -o "StrictHostKeyChecking no" $SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_USERNAME@$osm_host /bin/bash <<- EOT
    echo "" > /tmp/${stderr_file}
    cd $COMPOSE_BIN_FOLDER
    echo "${SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_PASSWORD}" | sudo -S docker-compose stop 2> /tmp/$stderr_file
    echo "${SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_PASSWORD}" | sudo -S docker-compose rm -f 2> /tmp/$stderr_file
    echo "${SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_PASSWORD}" | sudo -S docker-compose pull 2> /tmp/$stderr_file
    echo "${SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_PASSWORD}" | sudo -S docker-compose up -d 2> /tmp/$stderr_file
EOT

echo "get stderr from remote host"
scp -P $osm_host_ssh_port -o "StrictHostKeyChecking no" $SEU_JENKINS_ITERATEC_AT_OSM_HOSTS_USERNAME@$osm_host:"/tmp/${stderr_file}" .
echo "-> stderr create folder START"
cat ./$stderr_file
echo "-> stderr create folder END"