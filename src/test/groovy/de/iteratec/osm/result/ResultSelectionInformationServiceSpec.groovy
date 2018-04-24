package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.*

@Build([UserTiming, EventResult, Page, JobGroup, Browser, Location, MeasuredEvent, ConnectivityProfile])
class ResultSelectionInformationServiceSpec extends Specification implements BuildDataTest,
        ServiceUnitTest<ResultSelectionInformationService> {
    Page page
    JobGroup jobGroup
    Browser browser
    Location location
    MeasuredEvent measuredEvent
    ConnectivityProfile connectivityProfile

    void setupSpec() {
        mockDomains(UserTiming, UserTimingSelectionInformation, EventResult, ResultSelectionInformation, Page, JobGroup,
                Browser, Location, MeasuredEvent, ConnectivityProfile)
    }

    void "test userTimingInformation are relevant and unique"(relevantEventResults, relevantUserTimingsPerType, expectedResultSize, irrelevantEventResults, irrelevantUserTimings) {
        setup: "dates are set"
        DateTime relevantDate = new DateTime(DateTimeZone.UTC)
        DateTime irrelevantDate = relevantDate.minusDays(2)
        DateTime intervalStart = relevantDate.minusDays(1)
        DateTime intervalEnd = relevantDate

        when: "eventResults with usertimings are created"
        createEventResults(irrelevantEventResults, irrelevantDate, createUserTimingMarksAndMeasures(irrelevantUserTimings))
        createEventResults(relevantEventResults, relevantDate, createUserTimingMarksAndMeasures(relevantUserTimingsPerType))
        def groupedResults = ["not needed for this test", page, measuredEvent, jobGroup, location, browser, connectivityProfile, null, false]

        then: "unique UserTimingSelectionInfomation objects are returned"
        List<UserTimingSelectionInformation> testResult = service.getUserTimingSelectionInfosForGroupedEventResult(groupedResults, intervalStart, intervalEnd)
        if(expectedResultSize != 0){
            testResult.size() == expectedResultSize
            testResult.findAll {it.type == UserTimingType.MARK}.size() == relevantUserTimingsPerType
            testResult.findAll {it.type == UserTimingType.MEASURE}.size() == relevantUserTimingsPerType
        } else{
            testResult == null
        }

        where:
        relevantEventResults | relevantUserTimingsPerType | expectedResultSize | irrelevantEventResults | irrelevantUserTimings
        4                    | 2                          | 4                  | 6                      | 3
        4                    | 2                          | 4                  | 6                      | 0
        4                    | 2                          | 4                  | 0                      | 3
        4                    | 1                          | 2                  | 6                      | 3
        1                    | 4                          | 8                  | 6                      | 3
        0                    | 4                          | 0                  | 6                      | 3
        4                    | 0                          | 0                  | 6                      | 3
    }

    @Ignore
    void "test eventResult grouping"(){
        setup: "dates are set"
        DateTime relevantDate = new DateTime(DateTimeZone.UTC)
        DateTime irrelevantDate = relevantDate.minusDays(2)
        DateTime intervalStart = relevantDate.minusDays(1)
        DateTime intervalEnd = relevantDate

        when: "eventResults with usertimings are created"
        createEventResults(10, irrelevantDate, null)
        createEventResults(15, relevantDate, null)

        then:
        service.getGroupedEventResults(intervalStart, intervalEnd).size() == 1
    }

    void createEventResults(int amount,DateTime date, List<UserTiming> userTimings) {
        page = Page.build()
        jobGroup = JobGroup.build()
        browser = Browser.build()
        location = Location.build()
        measuredEvent = MeasuredEvent.build()
        connectivityProfile = ConnectivityProfile.build()

        amount.times {
            EventResult.build(
                    jobGroup: jobGroup,
                    page: page,
                    measuredEvent: measuredEvent,
                    browser: browser,
                    location: location,
                    connectivityProfile: connectivityProfile,
                    jobResultDate: date.toDate(),
                    userTimings: userTimings
            )
        }
    }


    List<UserTiming> createUserTimingMarksAndMeasures(int amountPerType){
        if(amountPerType == 0){
            return  null
        }

        List<UserTiming> userTimingList = []
        amountPerType.times{
            userTimingList.push(
                    UserTiming.build(name: "mark${it}", type: UserTimingType.MARK)
            )
        }
        amountPerType.times{
            userTimingList.push(
                    UserTiming.build(name: "mark${it}", type: UserTimingType.MEASURE, duration: it)
            )
        }
        return userTimingList
    }
}