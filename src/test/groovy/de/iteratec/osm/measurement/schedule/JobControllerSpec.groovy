package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.ConfigService
import de.iteratec.osm.InMemoryConfigService
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.util.I18nService
import de.iteratec.osm.util.PerformanceLoggingService
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.plugins.taggable.Tag
import grails.plugins.taggable.TagLink
import grails.plugins.taggable.TaggableService
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import spock.lang.Specification

@Build([Job])
class JobControllerSpec extends Specification implements BuildDataTest, ControllerUnitTest<JobController> {

    def setup() {
        Holders.applicationContext.metaClass.taggableService = new TaggableService()
        controller.configService = Mock(ConfigService)
        controller.i18nService = Mock(I18nService)
        controller.performanceLoggingService = Mock(PerformanceLoggingService)
        controller.jobDaoService = Mock(JobDaoService)
        controller.inMemoryConfigService = Mock(InMemoryConfigService)
        controller.jobService = Mock(JobService)
    }

    void setupSpec() {
        mockDomains(Tag, TagLink, ConnectivityProfile, Script)
    }

    void bindData(Job job) {
        job.properties.each { k, _ ->
            controller.params[k] = job[k]
        }
        if(job.executionSchedule){
            controller.params["executionSchedule"]= job.executionSchedule.substring(2,job.executionSchedule.length())
        }
    }

    def "test copy of an existing job"() {
        given: "a job"
        Job job = Job.build(label: "label", executionSchedule:  "0 0 7-22 * * ? *")
        String newLabel = "new Label"
        List newTagNames = ["newTag1", "newTag2"]
        when: "we create a copy of the job"
        bindData(job)
        controller.params.remove('id')
        controller.params["label"] = newLabel
        controller.params["tags"] = newTagNames
        controller.save()
        Job copy = Job.get(job.id + 1)
        List<Tag> tags = findTags(copy)
        then: "there should be a new job, with the new attributes"
        copy
        copy.location == job.location
        copy.label == newLabel
        copy.executionSchedule == job.executionSchedule
        copy.script == job.script
        tags*.name.sort() == newTagNames.sort()
    }

    def "test that creation of a copy doesn't affect the original"() {
        given: "a job"
        Job job = Job.build()
        when: "the params are used to create a copy"
        bindData(job)
        controller.params.remove("id")
        controller.params["label"] = "that's a different label"
        controller.save()
        then: "a job similar to the original should exist"
        Job.get(job.id) == job
    }

    def "test retrieve job template without params"(){
        when: "a job without params is created"
        def resp = controller.create()
        then: "the responding template shouldn't have any variables set"
        resp.job
        !resp.job.label
        !resp.job.executionSchedule
        !resp.job.deleted
        !resp.job.location
    }

    def "test retrieve job template with params"(){
        given: "some params for a job"
        String label = "a label"
        params.label = label
        String executionSchedule = "0 0 7-22 * * ? *"
        params.executionSchedule = executionSchedule
        when: "a job with these params is created"
        def resp = controller.create()
        then: "the responding template should have all the defined variables"
        resp.job.label == label
        resp.job.executionSchedule == executionSchedule
        !resp.job.deleted
        !resp.job.location
    }

    def "test update"(){
        given: "An existing job"
        Job job = Job.build(label: "oldLabel", executionSchedule: "0 0 7-22 * * ? *")
        String newLabel = "new label"
        job.label = newLabel
        bindData(job)
        params.id = job.id
        when: "an update with a new label is called"
        controller.update()
        then: "the jobs label should be changed to the new one"
        Job.get(job.id).label == newLabel
    }

    def "test delete"(){
        given: "An existing job"
        controller.jobService = new JobService()
        Job job = Job.build()
        params.id = job.id
        when: "the controller should delete the job"
        controller.delete()
        then: "the job should be marked as deleted, but still exist"
        Job.get(job.id).deleted
    }

    /**
     * Somehow we can't find tags via job.tags within units tests. Therefore we simple query them
     * @param job
     * @return
     */
    List<Tag> findTags(Job job) {
        TagLink.findAllByTagRefAndType(job.id as long, Job.class.simpleName.toLowerCase()).collect { it.tag }
    }
}
