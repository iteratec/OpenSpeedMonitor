package de.iteratec.osm.result.dao

import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.CachedView
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.result.SelectedMeasurand
import de.iteratec.osm.result.UserTiming
import de.iteratec.osm.result.UserTimingType
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

@Integration
@Rollback
class EventResultQueryBuilderMedianIntegrationSpec extends NonTransactionalIntegrationSpec{

    Browser browser
    Location location
    JobGroup jobGroup
    JobResult jobResult
    MeasuredEvent measuredEvent
    Page page
    ConnectivityProfile connectivityProfile

    void "check median for measurands"() {
        given: "two matching and 10 other Eventresults"
        EventResult.withNewSession { session ->
            connectivityProfile = ConnectivityProfile.build(name: "my-name")
            page = Page.build()
            jobGroup = JobGroup.build()

            10.times {
                EventResult.build(
                        connectivityProfile: connectivityProfile,
                        page: page,
                        jobGroup: jobGroup,
                        fullyLoadedTimeInMillisecs: 200,
                        medianValue: true,
                        userTimings: [UserTiming.build(name: "mark1", duration: new Double(200), type: UserTimingType.MEASURE)]
                )
            }
            2.times {
                EventResult.build(
                        connectivityProfile: connectivityProfile,
                        fullyLoadedTimeInMillisecs: 200,
                        medianValue: true,
                        userTimings: [UserTiming.build(name: "mark2", duration: new Double(100), type: UserTimingType.MEASURE)]
                )
            }
            session.flush()
        }


        when: "the builder is configured for measurands"
        SelectedMeasurand selectedMeasurand =  new SelectedMeasurand("FULLY_LOADED_TIME", CachedView.UNCACHED)
        def result = new EventResultQueryBuilder(0, 1000).withPageIdsIn([page.id])
                .withSelectedMeasurands([selectedMeasurand])
                .getMedianData()

        then: "only both matching Eventresults are found"
        result.size() == 1
        result.every {
            it.fullyLoadedTimeInMillisecs == 200
        }
    }
}
