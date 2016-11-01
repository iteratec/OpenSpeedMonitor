#!/bin/bash
set -x

echo "running deployment script on target host"
echo "####################################################"
stderr_file="osm_${bamboo_tomcat_SSH_PORT_TO_HOST_TO_DEPLOY_TO}_run_deployment_script_on_target_host_stderr.txt"
echo "run on target host:"
echo "sudo /opt/osm_deployment.sh $bamboo_tomcat_PORT_TO_DEPLOY /home/$bamboo_tomcat_JENKINS_USER_NAME $bamboo_tomcat_EXTERNAL_OSM_CONFIG_FOLDER $bamboo_tomcat_EXTERNAL_OSM_CONFIG_FILE $bamboo_tomcat_TOMCAT_SERVER_PORT_TO_WAIT_FOR $bamboo_tomcat_TOMCAT_AJP_PORT_TO_WAIT_FOR $bamboo_tomcat_TOMCAT_TLS_PORT_TO_WAIT_FOR"
ssh -p $bamboo_tomcat_SSH_PORT_TO_HOST_TO_DEPLOY_TO -o "StrictHostKeyChecking no" $bamboo_tomcat_JENKINS_USER_NAME@$bamboo_tomcat_HOST_TO_DEPLOY_TO /bin/bash <<EOT
sudo /opt/osm_deployment.sh $bamboo_tomcat_PORT_TO_DEPLOY /home/$bamboo_tomcat_JENKINS_USER_NAME $bamboo_tomcat_EXTERNAL_OSM_CONFIG_FOLDER $bamboo_tomcat_EXTERNAL_OSM_CONFIG_FILE $bamboo_tomcat_TOMCAT_SERVER_PORT_TO_WAIT_FOR $bamboo_tomcat_TOMCAT_AJP_PORT_TO_WAIT_FOR $bamboo_tomcat_TOMCAT_TLS_PORT_TO_WAIT_FOR 2> /tmp/${stderr_file}
EOT
scp -P $bamboo_tomcat_SSH_PORT_TO_HOST_TO_DEPLOY_TO -o "StrictHostKeyChecking no" $bamboo_tomcat_JENKINS_USER_NAME@$bamboo_tomcat_HOST_TO_DEPLOY_TO:"/tmp/$stderr_file" .
echo "stderr_file START"
cat "./$stderr_file"
echo "stderr_file END"