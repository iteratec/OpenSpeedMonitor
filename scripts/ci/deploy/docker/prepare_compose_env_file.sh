#!/usr/bin/env bash

# Exit  immediately  if  a pipeline (which may consist of a single simple command), a list, or a compound command
# (see SHELL GRAMMAR above),  exits with a non-zero status.
set -e
# After expanding each simple command, for command, case command, select command, or arithmetic for command,
# display the expanded value of PS4, followed by the command and its expanded  arguments  or  associated word list.
#set -x

COMPOSE_BIN_FOLDER="~/osm"
ENV_OSM_FILE=./docker/.env_osm

echo "prepare environment-vars file for compose"
echo "####################################################"

echo "MYSQL_HOST=osm_mysql" > $ENV_OSM_FILE

echo "MYSQL_DATABASE=$MYSQL_DATABASE" >> $ENV_OSM_FILE
echo "MYSQL_USER=$MYSQL_USER" >> $ENV_OSM_FILE
echo "MYSQL_PASSWORD=$MYSQL_PASSWORD" >> $ENV_OSM_FILE
echo "OSM_ADMIN_USER=$OSM_ADMIN_USER" >> $ENV_OSM_FILE
echo "OSM_ADMIN_PASSWORD=$OSM_ADMIN_PASSWORD" >> $ENV_OSM_FILE
echo "OSM_ROOT_USER=$OSM_ROOT_USER" >> $ENV_OSM_FILE
echo "OSM_ROOT_PASSWORD=$OSM_ROOT_PASSWORD" >> $ENV_OSM_FILE
echo "DETAIL_ANALYSIS_URL=$DETAIL_ANALYSIS_URL" >> $ENV_OSM_FILE
echo "ENABLE_DETAIL_ANALYSIS=$ENABLE_DETAIL_ANALYSIS" >> $ENV_OSM_FILE
echo "VIRTUAL_HOST=$VIRTUAL_HOST" >> $ENV_OSM_FILE

echo "copy environment-vars file to target host"
echo "####################################################"
scp -P $SSH_PORT_TO_HOST_DOCKER -o "StrictHostKeyChecking no" $ENV_OSM_FILE $OSM_HOSTS_OS_USER@$HOST_DOCKER:"${COMPOSE_BIN_FOLDER}/"