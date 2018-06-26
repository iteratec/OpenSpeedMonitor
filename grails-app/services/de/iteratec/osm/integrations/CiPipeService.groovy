package de.iteratec.osm.integrations

import de.iteratec.osm.measurement.schedule.Job
import grails.gorm.transactions.Transactional

@Transactional
class CiPipeService {

    String getCiIntegrationScriptFor(Job job) {

        return this.class.classLoader.getResourceAsStream('OsmCiPipeCheck.groovy.template').text
            .replace('{{jobId}}', String.valueOf(job.ident()))
            .replace('{{wptServerBaseUrl}}', job?.location?.wptServer?.baseUrl)
    }
}
