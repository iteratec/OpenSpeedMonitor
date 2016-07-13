FROM java:openjdk-8
MAINTAINER nils.kuhn@iteratec.de, birger.kamp@iteratec.de

ENV OSM_VERSION 3.4.8-build293
ENV OSM_HOME /osm
ENV OSM_CONFIG_HOME /home/osm/.grails
ENV JAVA_OPTS "-server -Dgrails.env=prod -Dfile.encoding=UTF-8"
ENV DOCKERIZE_VERSION v0.2.0

# add osm-user
RUN useradd -ms /bin/bash osm

# install dockerize
RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

# get osm-sources and build war-file
RUN mkdir -p $OSM_HOME $OSM_HOME/logs $OSM_CONFIG_HOME
WORKDIR $OSM_HOME
ADD ./build/libs/OpenSpeedMonitor*.war $OSM_HOME/OpenSpeedMonitor.war

# add osm config file
ADD docker/templates/osm-config.yml.j2 $OSM_CONFIG_HOME/OpenSpeedMonitor-config.yml.j2

# add entrypoint script
ADD docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh && \
    chown osm:osm -R $OSM_HOME $OSM_CONFIG_HOME

USER osm
VOLUME ["$OSM_CONFIG_HOME","$OSM_HOME/logs"]
EXPOSE 8080
ENTRYPOINT /entrypoint.sh
