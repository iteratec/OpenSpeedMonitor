package de.iteratec.osm.result.detail

import de.iteratec.osm.ConfigService
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.JobResult
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Test
import spock.lang.Specification

@TestFor(HarFetchService)
@Mock([JobGroup, Job, HARJob, Script, WebPageTestServer, Browser, Location, JobResult])
class HarFetchServiceTests extends Specification{

    def setup(){
        service.metaClass.fetch = {}
        service.configService = new ConfigService()
        service.configService.metaClass.isDetailFetchingEnabled = {true}
        service.metaClass.shouldPersistHar = {true}
    }

    @Test
    public void testJobGroupPersist(){
        given:
            Job job = TestDataUtil.createSimpleJob()
            job.jobGroup.persistHar = true
            TestDataUtil.createJobResult("1", new Date(), job, job.location).save()

        when:
            service.addJobResultToQueue(job.id)

        then:
            HARJob.count() == 1
    }

    @Test
    public void testJobGroupNotPersist(){
        given:
            Job job = TestDataUtil.createSimpleJob()
            job.jobGroup.persistHar = false
            TestDataUtil.createJobResult("1", new Date(), job, job.location).save()

        when:
            service.addJobResultToQueue(job.id)

        then:
            HARJob.count() == 0
    }
}
