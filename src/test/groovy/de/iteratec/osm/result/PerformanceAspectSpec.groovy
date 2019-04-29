package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import spock.lang.Specification

@Build([JobGroup, Page, PerformanceAspect])
class PerformanceAspectSpec extends Specification implements BuildDataTest {
    Page page1, page2
    JobGroup jobGroup1, jobGroup2
    SelectedMeasurand metric

    def setup() {
        page1 = Page.build()
        jobGroup1 = JobGroup.build()
        page2 = Page.build()
        jobGroup2 = JobGroup.build()
        metric = new SelectedMeasurand(Measurand.DOC_COMPLETE_TIME.name(), CachedView.CACHED)
    }

    def cleanup() {
    }

    void "ensure duplicates are not allowed"() {
        given: "there is a performance aspect"

        PerformanceAspect.build(
                page: page1,
                jobGroup: jobGroup1,
                performanceAspectType: PerformanceAspectType.PAGE_CONSTRUCTION_STARTED,
                metric: metric).save(flush: true)

        when: "a potential duplicate to the first aspect is created"
        PerformanceAspect duplicateAspect = new PerformanceAspect(page: Page.findById(inputPageId), jobGroup: JobGroup.findById(inputJobGroupId), performanceAspectType: performanceAspectType, metric: metric)

        then: "its validation only fails if it is really a duplicate"
        duplicateAspect.validate() == valid

        where: "there are real duplicates and no real duplicates"
        inputPageId | inputJobGroupId | performanceAspectType                           | valid
        1           | 1               | PerformanceAspectType.PAGE_CONSTRUCTION_STARTED | false
        2           | 1               | PerformanceAspectType.PAGE_CONSTRUCTION_STARTED | true
        1           | 2               | PerformanceAspectType.PAGE_CONSTRUCTION_STARTED | true
        1           | 1               | PerformanceAspectType.PAGE_SHOWS_USEFUL_CONTENT | true
    }

    void "ensure metric survives database"() {
        given: "there is a performance aspect"
        PerformanceAspect.build(
                page: page1,
                jobGroup: jobGroup1,
                performanceAspectType: PerformanceAspectType.PAGE_CONSTRUCTION_STARTED,
                metric: metric).save(flush: true)

        when: "this aspect is retrieved from the database"
        PerformanceAspect foundAspect = PerformanceAspect.findByPageAndJobGroup(page1, jobGroup1)

        then: "metric stays the same as before"
        foundAspect.metric == metric
    }
}
