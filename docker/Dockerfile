FROM java:8
MAINTAINER nils.kuhn@iteratec.de, birger.kamp@iteratec.de

ENV OSM_VERSION 3.4.8-build293
ENV OSM_HOME /osm
ENV OSM_CONFIG_HOME /home/osm/.grails
ENV JAVA_OPTS "-server -Dgrails.env=prod -Dfile.encoding=UTF-8"

# get OSM-sources
RUN mkdir -p $OSM_HOME
WORKDIR /osm
#RUN wget https://github.com/IteraSpeed/OpenSpeedMonitor/archive/$OSM_VERSION.zip && \
#    unzip $OSM_VERSION.zip -d /osm && \
#    mv OpenSpeedMonitor-$OSM_VERSION/* . && \
#    rm -rf $OSM_VERSION.zip OpenSpeedMonitor-$OSM_VERSION
RUN wget https://github.com/IteraSpeed/OpenSpeedMonitor/archive/feature/detailAnalysis.zip && \
    unzip detailAnalysis.zip -d $OSM_HOME && \
    rm -rf detailAnalysis.zip

# build OSM-jar
WORKDIR $OSM_HOME/OpenSpeedMonitor-feature-detailAnalysis
RUN ./gradlew assemble && \
    mv ./build/libs/*.war $OSM_HOME/OpenSpeedMonitor.war
WORKDIR $OSM_HOME
RUN rm -r $OSM_HOME/OpenSpeedMonitor-feature-detailAnalysis /root/.gradle

# add osm config file
RUN useradd -ms /bin/bash osm
RUN mkdir -p $OSM_HOME/logs && \
    chmod 777 -R $OSM_HOME/logs
ADD templates/osm-config.yml $OSM_CONFIG_HOME/OpenSpeedMonitor-config.yml
RUN mkdir -p $OSM_HOME/logs && \
    chown osm:osm -R $OSM_HOME $OSM_CONFIG_HOME
VOLUME ["$OSM_CONFIG_HOME","$OSM_HOME/logs"]
EXPOSE 8080

# add entrypoint script
ADD entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

USER osm
ENTRYPOINT /entrypoint.sh
