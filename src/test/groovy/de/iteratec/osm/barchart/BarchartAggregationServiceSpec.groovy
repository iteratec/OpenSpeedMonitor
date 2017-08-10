package de.iteratec.osm.barchart

import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.Measurand
import de.iteratec.osm.result.Selected
import de.iteratec.osm.result.SelectedType
import de.iteratec.osm.result.UserTiming
import de.iteratec.osm.result.UserTimingType
import de.iteratec.osm.util.I18nService
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.junit.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(BarchartAggregationService)
@Mock([EventResult, UserTiming, JobGroup, Page])
@Build([EventResult, UserTiming, JobGroup, Page])
class BarchartAggregationServiceSpec extends Specification {
    @Shared
    Page page1
    @Shared
    Page page2
    @Shared
    JobGroup jobGroup1
    @Shared
    JobGroup jobGroup2

    def setup(){
        mockI18NServices()
        mockOsmConfigCacheService()

        page1 = Page.build(
                name: "my first page"
        )
        page2 = Page.build(
                name: "my second page"
        )
        jobGroup1 = JobGroup.build(
                name: "my first jobGroup"
        )
        jobGroup2 = JobGroup.build(
                name: "my second jobGroup"
        )
    }

    void "test query and aggregation"() {
        given:"EventResults are build"
        Selected selected = new Selected(selectedMeasurand, CachedView.UNCACHED)
        boolean isMeasurand = selected.selectedType == SelectedType.MEASURAND
        DateTime from = new DateTime(2016, 8, dayStart, 14, 6)
        DateTime to = new DateTime(2016, 8, 20, 14, 6)
        amount.times{
            EventResult eventResult = buildForEventResult(page1, jobGroup1, dayStart + it )
            setValueAtEventResult(eventResult, 5, selected)
        }
        amount.times{
            EventResult eventResult = buildForEventResult(page1, jobGroup2, dayStart + it )
            setValueAtEventResult(eventResult, 10, selected)
        }
        amount.times{
            EventResult eventResult = buildForEventResult(page2, jobGroup2, dayStart + it )
            setValueAtEventResult(eventResult, 15, selected)
        }
        amount.times{
            EventResult eventResult = buildForEventResult(page2, jobGroup1, dayStart + it )
            setValueAtEventResult(eventResult, 20, selected)
        }


        when:"query is executed"
        List<BarchartSeries> testee = service.aggregateForMeasurandOrUserTiming(groupProperties, [selected], new GetBarchartCommand(from: from, to: to), allJobGroups, allPages, isMeasurand)


        then:"result is as expected"
        testee.size() == expectedSize
        testee[0].value == expectedValue
        testee[0].valueComparative == null

        where:
        amount |expectedSize |expectedValue | dayStart | selectedMeasurand             |  groupProperties     | allJobGroups            | allPages
        10     |1            | 5            | 10       | Measurand.DOM_TIME.toString() | ["jobGroup", "page"] | [jobGroup1]             | [page1]
        10     |1            | 10           | 10       | Measurand.DOM_TIME.toString() | ["page", "jobGroup"] | [jobGroup2]             | [page1]
        10     |1            | 15           | 10       | Measurand.DOM_TIME.toString() | ["jobGroup", "page"] | [jobGroup2]             | [page2]
        10     |1            | 20           | 10       | Measurand.DOM_TIME.toString() | ["page", "jobGroup"] | [jobGroup1]             | [page2]
        10     |1            | 40           | 10       | Measurand.DOM_TIME.toString() | ["jobGroup", "page"] | [jobGroup1, jobGroup2]  | [page1, page2]
    }


    EventResult buildForEventResult(Page page, JobGroup jobGroup, int day){
        Date from = new DateTime(2016, 8, day, 14, 6).toDate()
        return EventResult.build(
                page: page,
                jobGroup: jobGroup,
                jobResultDate: from,
                fullyLoadedTimeInMillisecs: 10
        )

    }
    void setValueAtEventResult(EventResult eventResult, def relevantValue, Selected relevantMeasurand){
        if(relevantMeasurand.selectedType == SelectedType.MEASURAND){
            String eventResultFieldname = relevantMeasurand.getDatabaseRelevantName()
            eventResult."$eventResultFieldname" = relevantValue
        }
        if(relevantMeasurand.selectedType == SelectedType.USERTIMING_MARK){
            UserTiming userTiming = UserTiming.build(
                    name: relevantMeasurand.getDatabaseRelevantName(),
                    type: UserTimingType.MARK,
                    startTime: relevantValue
            )
            if(eventResult.userTimings){
                eventResult.userTimings.add(userTiming)
            }else{
                eventResult.userTimings = [userTiming]
            }
        }
        if(relevantMeasurand.selectedType == SelectedType.USERTIMING_MEASURE){
            UserTiming userTiming = UserTiming.build(
                    name: relevantMeasurand.getDatabaseRelevantName(),
                    type: UserTimingType.MEASURE,
                    duration: relevantValue
            )
            if(eventResult.userTimings){
                eventResult.userTimings.add(userTiming)
            }else{
                eventResult.userTimings = [userTiming]
            }
        }
    }

    def mockI18NServices() {
        service.i18nService = Stub(I18nService) {
            msg(_, _, _) >> { args ->
                return args[1]
            }
        }
    }

    def mockOsmConfigCacheService() {
        service.osmConfigCacheService = Stub(OsmConfigCacheService) {
            getMinValidLoadtime() >> 10
            getMaxValidLoadtime() >> 1000
        }
    }
}
