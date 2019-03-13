package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import spock.lang.Specification

@Build([JobGroup, Page, PerformanceAspect])
class PerformanceAspectSpec extends Specification implements BuildDataTest {
    Page page
    JobGroup jobGroup
    SelectedMeasurand metric

    def setup() {
        page = Page.build()
        jobGroup = JobGroup.build()
        metric = new SelectedMeasurand(Measurand.DOC_COMPLETE_TIME.name(), CachedView.CACHED)
    }

    def cleanup() {
    }

    void "ensure duplicates are not allowed"() {
        given: "there is a performance aspect"
        PerformanceAspect.build(
                page: page,
                jobGroup: jobGroup,
                performanceAspectType: PerformanceAspectType.PAGE_CONSTRUCTION_STARTED,
                metric: metric).save(flush:true)

        when: "a duplicate to the first aspect is created"
        PerformanceAspect duplicateAspect = new PerformanceAspect(page: page, jobGroup: jobGroup, performanceAspectType: PerformanceAspectType.PAGE_CONSTRUCTION_STARTED, metric: metric)

        then: "the duplicate's validation fails"
        !duplicateAspect.validate()
    }

    void "ensure metric survives database"(){
        given: "there is a performance aspect"
        PerformanceAspect.build(
                page: page,
                jobGroup: jobGroup,
                performanceAspectType: PerformanceAspectType.PAGE_CONSTRUCTION_STARTED,
                metric: metric).save(flush:true)

        when: "this aspect is retrieved from the database"
        PerformanceAspect foundAspect = PerformanceAspect.findByPageAndJobGroup(page,jobGroup)

        then: "metric stays the same as before"
        foundAspect.metric == metric
    }
}
