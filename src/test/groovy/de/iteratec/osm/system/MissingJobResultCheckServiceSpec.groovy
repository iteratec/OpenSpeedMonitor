package de.iteratec.osm.system

import de.iteratec.osm.result.JobResult
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import org.quartz.CronExpression
import spock.lang.Specification

@Build([JobResult])
class MissingJobResultCheckServiceSpec extends Specification implements BuildDataTest,
        ServiceUnitTest<MissingJobResultCheckService>{

    private List<DateTime> mockDates = []

    def setup() {
        mockDates.add( new DateTime(2018, 11, 1, 8, 00) )
        mockDates.add( new DateTime(2018, 11, 2, 8, 00) )
        mockDates.add( new DateTime(2018, 11, 3, 8, 00) )
        mockDates.add( new DateTime(2018, 11, 4, 8, 00) )
    }

    def cleanup() {
    }

    void "Find corresponding Date"() {
        given:
        JobResult jobResult = new JobResult(executionDate: mockDates[0].toDate())

        when:
        DateTime date = service.findDateForJobResult(mockDates, jobResult)

        then:
        date == mockDates[0]
    }

    void "Find corresponding Date with slight offset"() {
        given:
        int dateNr = 1
        JobResult jobResult = new JobResult(executionDate: mockDates[dateNr].plusMinutes(1).toDate())

        when:
        DateTime date = service.findDateForJobResult(mockDates, jobResult)

        then:
        date == mockDates[dateNr]
    }

    void "Find no corresponding Date"() {
        given:
        JobResult jobResult = new JobResult(executionDate: mockDates[3].plusHours(1).toDate())

        when:
        DateTime date = service.findDateForJobResult(mockDates, jobResult)

        then:
        date == null
    }

    void "Find just no corresponding Date"() {
        given:
        JobResult jobResult = new JobResult(executionDate: mockDates[3].plusMinutes(
                MissingJobResultCheckService.SCHEDULE_THRESHOLD_MINUTES.toInteger()).toDate())

        when:
        DateTime date = service.findDateForJobResult(mockDates, jobResult)

        then:
        date == null
    }

    void "Get correct scheduled Dates list"() {
        given:
        DateTime from = new DateTime(2018, 11, 1, 11, 45)
        DateTime to = new DateTime(2018, 11, 1, 14, 15)
        CronExpression cron = new CronExpression("0 0/30 * ? * *")
        def expectedDates = [new DateTime(2018, 11, 1, 12, 0),
                         new DateTime(2018, 11, 1, 12, 30),
                         new DateTime(2018, 11, 1, 13, 0),
                         new DateTime(2018, 11, 1, 13, 30),
                         new DateTime(2018, 11, 1, 14, 0)]

        when:
        def dates = service.getScheduledDates(from.toDate(), to.toDate(), cron)

        then:
        dates == expectedDates
    }

    void "correctDateDist"() {
        given:
        DateTime d1 = new DateTime(2018, 11, 1, 10, 00)
        DateTime d2 = new DateTime(2018, 11, 1, 9, 42)

        when:
        long dist = service.dateDist(d1, d2)

        then:
        dist == 18L * 60L * 1000L
    }
}
