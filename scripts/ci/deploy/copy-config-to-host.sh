#!/bin/bash
set -e

echo "create directory for config on target host"
echo "####################################################"
ssh -p $bamboo_tomcat_SSH_PORT_TO_HOST_TO_DEPLOY_TO -o "StrictHostKeyChecking no" $bamboo_tomcat_JENKINS_USER_NAME@$bamboo_tomcat_HOST_TO_DEPLOY_TO \
	"mkdir -p /home/${bamboo_tomcat_JENKINS_USER_NAME}/${bamboo_tomcat_EXTERNAL_OSM_CONFIG_FOLDER}/"

echo "copy config to target host"
echo "####################################################"
stderr_file="osm_${bamboo_tomcat_SSH_PORT_TO_HOST_TO_DEPLOY_TO}_copy-cfg-to-server-stderr.txt"
echo "stderr_file=${stderr_file}"
echo "config-file-source=./credentials/$bamboo_tomcat_EXTERNAL_OSM_CONFIG_FOLDER/$bamboo_tomcat_EXTERNAL_OSM_CONFIG_FILE"
echo "config-file-target=${bamboo_tomcat_JENKINS_USER_NAME}@${bamboo_tomcat_HOST_TO_DEPLOY_TO}:/home/${bamboo_tomcat_JENKINS_USER_NAME}/${bamboo_tomcat_EXTERNAL_OSM_CONFIG_FOLDER}/${bamboo_tomcat_EXTERNAL_OSM_CONFIG_FILE}"
scp -r -P "$bamboo_tomcat_SSH_PORT_TO_HOST_TO_DEPLOY_TO" -o "StrictHostKeyChecking no" \
    "./credentials/$bamboo_tomcat_EXTERNAL_OSM_CONFIG_FOLDER/$bamboo_tomcat_EXTERNAL_OSM_CONFIG_FILE" "${bamboo_tomcat_JENKINS_USER_NAME}@${bamboo_tomcat_HOST_TO_DEPLOY_TO}:/home/${bamboo_tomcat_JENKINS_USER_NAME}/${bamboo_tomcat_EXTERNAL_OSM_CONFIG_FOLDER}/${bamboo_tomcat_EXTERNAL_OSM_CONFIG_FILE} 2> /tmp/${stderr_file}" \
    && scp -P $bamboo_tomcat_SSH_PORT_TO_HOST_TO_DEPLOY_TO -o "StrictHostKeyChecking no" $bamboo_tomcat_JENKINS_USER_NAME@$bamboo_tomcat_HOST_TO_DEPLOY_TO:"/tmp/$stderr_file" .
echo "stderr_file START"
cat "./$stderr_file"
echo "stderr_file END"