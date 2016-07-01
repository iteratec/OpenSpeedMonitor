#!/bin/bash

if [ -z "$OSM_URL" ]
then
  OSM_URL="http://localhost:8080"
fi
if [ -z "$OSM_ADMIN_USER" ]
then
  OSM_ADMIN_USER="admin"
fi
if [ -z "$OSM_ADMIN_PASSWORD" ]
then
  OSM_ADMIN_PASSWORD="secret123"
fi
if [ -z "$OSM_ROOT_USER" ]
then
  OSM_ROOT_USER="root"
fi
if [ -z "$OSM_ROOT_PASSWORD" ]
then
  OSM_ROOT_PASSWORD="muchMoreSecret!123"
fi
if [ -z "$MYSQL_HOST" ]
then
  MYSQL_HOST="osm-mysql"
fi
if [ -z "$MYSQL_DATABASE" ]
then
  MYSQL_DATABASE="osm"
fi
if [ -z "$MYSQL_USER" ]
then
  MYSQL_USER="osm"
fi
if [ -z "$MYSQL_PASSWORD" ]
then
  MYSQL_PASSWORD="osm123"
fi

# prepare osm config respective set env variables
OSM_CONFIG_TARGET_LOCATION="${OSM_CONFIG_HOME}/OpenSpeedMonitor-config.yml"

echo "complete osm config file gets prepared"
sed -i -r "s,%OSM_URL%,${OSM_URL},g" "${OSM_CONFIG_TARGET_LOCATION}"
sed -i -r "s,%OSM_ADMIN_USER%,${OSM_ADMIN_USER},g" "${OSM_CONFIG_TARGET_LOCATION}"
sed -i -r "s,%OSM_ADMIN_PASSWORD%,${OSM_ADMIN_PASSWORD},g" "${OSM_CONFIG_TARGET_LOCATION}"
sed -i -r "s,%OSM_ROOT_USER%,${OSM_ROOT_USER},g" "${OSM_CONFIG_TARGET_LOCATION}"
sed -i -r "s,%OSM_ROOT_PASSWORD%,${OSM_ROOT_PASSWORD},g" "${OSM_CONFIG_TARGET_LOCATION}"
sed -i -r "s,%MYSQL_HOST%,${MYSQL_HOST},g" "${OSM_CONFIG_TARGET_LOCATION}"
sed -i -r "s,%MYSQL_DATABASE%,${MYSQL_DATABASE},g" "${OSM_CONFIG_TARGET_LOCATION}"
sed -i -r "s,%MYSQL_USER%,${MYSQL_USER},g" "${OSM_CONFIG_TARGET_LOCATION}"
sed -i -r "s,%MYSQL_PASSWORD%,${MYSQL_PASSWORD},g" "${OSM_CONFIG_TARGET_LOCATION}"


# start tomcat
#java -Dgrails.env=prod -jar $OSM_HOME/build/libs/OpenSpeedMonitor-$OSM_VERSION.war
java -Dgrails.env=prod -jar $OSM_HOME/OpenSpeedMonitor.war
