package de.iteratec.osm.result

import de.iteratec.osm.measurement.schedule.JobStatisticService
import grails.events.annotation.gorm.Listener
import grails.util.Environment
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
class JobResultInsertListener {
    @Autowired
    JobStatisticService jobStatisticService

    @Listener(JobResult)
    void onPreUpdateEvent(PreUpdateEvent event) {
        JobResult jobResult = event.entityObject as JobResult
        if ((Environment.getCurrent() != Environment.TEST) && jobResult.isDirty('httpStatusCode')) {
            jobStatisticService.updateStatsFor(jobResult.job)
        }
    }

    @Listener(JobResult)
    void onPostInsertEvent(PostInsertEvent event) {
        JobResult jobResult = event.entityObject as JobResult
        if (Environment.getCurrent() != Environment.TEST) {
            jobStatisticService.updateStatsFor(jobResult.job)
        }
    }
}
