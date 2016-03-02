#!/bin/bash
#################
# configure_deployment_*.sh-script must be run in front of this script
# else variables will be missing
#################

# make external config file available as env variable
if [ -e "./$EXTERNAL_OSM_CONFIG_FILE" ]
  then export EXTERNAL_OSM_CONFIG_FILE=$EXTERNAL_OSM_CONFIG_FILE
  else
    echo "External config file $EXTERNAL_OSM_CONFIG_FILE does not exist in osm_creadentials repository!"
    exit 1;
fi

# detect port to undeploy (tomcat with actual osm version) and to deploy (tomcat without osm)
ports=()
if(curl -u "${TOMCAT_ADMIN_USERNAME}:${TOMCAT_ADMIN_PASSWORD}" "http://${HOST_TO_DEPLOY_TO}:${TOMCAT_PORT1}/manager/text/list" | grep OpenSpeedMonitor);
    then ports[${#ports[*]}]=$TOMCAT_PORT1
fi
if(curl -u "${TOMCAT_ADMIN_USERNAME}:${TOMCAT_ADMIN_PASSWORD}" "http://${HOST_TO_DEPLOY_TO}:${TOMCAT_PORT2}/manager/text/list" | grep OpenSpeedMonitor);
    then ports[${#ports[*]}]=$TOMCAT_PORT2
fi
if [ ${#ports[*]} -eq 0 ];
    then
      export PORT_TO_DEPLOY=$TOMCAT_PORT1
      export TOMCAT_SERVER_PORT_TO_WAIT_FOR=$TOMCAT_SERVER_PORT1
      export TOMCAT_AJP_PORT_TO_WAIT_FOR=$TOMCAT_AJP_PORT1
      export TOMCAT_TLS_PORT_TO_WAIT_FOR=$TOMCAT_TLS_PORT1
      export PORT_TO_UNDEPLOY=-1
  elif [ ${#ports[*]} -eq 1 ];
    then
      export PORT_TO_UNDEPLOY=${ports[0]}
      ((PORT_TO_DEPLOY=PORT_TO_UNDEPLOY==$TOMCAT_PORT1 ? $TOMCAT_PORT2 : $TOMCAT_PORT1 ))
      ((TOMCAT_SERVER_PORT_TO_WAIT_FOR=PORT_TO_UNDEPLOY==$TOMCAT_PORT1 ? $TOMCAT_SERVER_PORT2 : $TOMCAT_SERVER_PORT1 ))
      ((TOMCAT_AJP_PORT_TO_WAIT_FOR=PORT_TO_UNDEPLOY==$TOMCAT_PORT1 ? $TOMCAT_AJP_PORT2 : $TOMCAT_AJP_PORT1 ))
      ((TOMCAT_TLS_PORT_TO_WAIT_FOR=PORT_TO_UNDEPLOY==$TOMCAT_PORT1 ? $TOMCAT_TLS_PORT2 : $TOMCAT_TLS_PORT1 ))
      export PORT_TO_DEPLOY
      export TOMCAT_SERVER_PORT_TO_WAIT_FOR
      export TOMCAT_AJP_PORT_TO_WAIT_FOR
      export TOMCAT_TLS_PORT_TO_WAIT_FOR
  else
    echo "OpenSpeedMonitor is deployed on both tomcats"
    exit 1;
fi
