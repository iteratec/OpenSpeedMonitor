package de.iteratec.osm.integrations

import de.iteratec.osm.measurement.schedule.Job
import grails.transaction.Transactional

@Transactional
class CiPipeService {

    String getCiIntegrationScriptFor(Job job) {

        return this.class.classLoader.getResourceAsStream('OsmCiPipeCheck.groovy.template').text
            .replace('{{jobId}}', String.valueOf(job.ident()))
            .replace('{{wptServerBaseUrl}}', job?.location?.wptServer?.baseUrl)

//        new BufferedReader(new InputStreamReader(templateStream))
//
//        return
//
//        Template template = new StreamingTemplateEngine().createTemplate(
//            new BufferedReader(new InputStreamReader(templateStream))
//        )
//        return template.make(
//            [
//                jobId: job.ident(),
//                wptServerBaseUrl: job?.location?.wptServer?.baseUrl
//            ].withDefault{ key -> return "\${${key}}" }
//        )
    }

}
