package de.iteratec.osm.result

import grails.databinding.SimpleMapDataBindingSource
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class EventResultDashboardShowAllCommandSpec extends Specification {

    def dataBinder

    def setup() {
        // Use Grails data binding
        dataBinder = applicationContext.getBean('grailsWebDataBinder')
    }


    def "Test databinding from different valid date params"() {
        given:
        // Date (year-1900, month[0-11], day[1-31])
        Date expectedFrom = new Date(116, 00, 03)
        Date expectedTo = new Date(116, 01, 05)
        SimpleMapDataBindingSource source = [from: from, to: to]
        EventResultDashboardShowAllCommand command = new EventResultDashboardShowAllCommand()

        when: "binding dates to command"
        dataBinder.bind(command, source)

        then: "the command has a valid date"
        command.from == expectedFrom
        command.to == expectedTo

        where: "the command gets different type of dates"
        from         | to
        "03.01.2016" | "05.02.2016"
        new Date(116, 00, 03) | new Date(116, 01, 05)
    }
}
