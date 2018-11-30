package de.iteratec.osm.system

import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import org.joda.time.DateTime
import spock.lang.Specification

@Build([MissingJobResultCheck])
class MissingJobResultCheckSpec extends Specification implements BuildDataTest {

    List<DateTime> mockDates = []

    def setup() {
        mockDomains(MissingJobResultCheck)

        mockDates.add(new DateTime(2018, 11, 1, 12, 00))
        mockDates.add(new DateTime(2018, 11, 2, 9, 00))
        mockDates.add(new DateTime(2018, 11, 1, 13, 00))
        mockDates.add(new DateTime(2018, 11, 2, 6, 00))

    }

    void "Return latest MissingJobResultDate"() {
        given:
        MissingJobResultCheck.build(date: mockDates[0].toDate(), missingResults: 5)
        MissingJobResultCheck.build(date: mockDates[1].toDate(), missingResults: 2)
        MissingJobResultCheck.build(date: mockDates[2].toDate(), missingResults: 11)
        MissingJobResultCheck.build(date: mockDates[3].toDate(), missingResults: 11)

        when:
        Date date = MissingJobResultCheck.last('date').date

        then:
        date == mockDates[1].toDate()
    }
}
