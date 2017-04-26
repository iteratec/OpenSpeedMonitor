package de.iteratec.osm.csi

import grails.databinding.SimpleMapDataBindingSource
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import org.joda.time.Interval
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class CsiDashboardShowAllCommandSpec extends Specification {

    def dataBinder

    def setup() {
        // Use Grails data binding
        dataBinder = applicationContext.getBean('grailsWebDataBinder')
    }

    def "Should warn about long processing time - weekly csi aggs interval two weeks"() {
        given: "Two weeks for 2 JobGroups, 5 Pages and 2 browsers of selected data and weekly csi aggs"
        int countOfSelectedBrowser = 2;
        DateTime start = new DateTime(2013, 9, 30, 0, 0);
        DateTime end = new DateTime(2013, 10, 13, 23, 59);
        Interval timeFrameTwoWeeks = new Interval(start, end);
        int selectedAggregationIntervallInMintues = 7 * 24 * 60; // one week
        SimpleMapDataBindingSource source = [selectedFolder: [1,2], selectedPages: [1,2,3,4,5]]
        CsiDashboardShowAllCommand command = new CsiDashboardShowAllCommand()

        when: "are given by the user in dashboard"
        dataBinder.bind(command, source)

        then: "A warning is provided about longer processing time"
        command.shouldWarnAboutLongProcessingTime(
            timeFrameTwoWeeks,
            selectedAggregationIntervallInMintues,
            countOfSelectedBrowser
        ) == false
    }

    def "Should not warn about long processing time - hourly csi aggs interval one year"() {
        given: "One year for 2 JobGroups, 5 Pages and 2 browsers of selected data and hourly csi aggs"
        int countOfSelectedBrowser = 2;
        DateTime start = new DateTime(2013, 9, 30, 0, 0);
        DateTime end = new DateTime(2014, 9, 30, 0, 0);
        Interval timeFrameOneYear = new Interval(start, end);
        int selectedAggregationIntervallInMintues = 60; // one hour
        SimpleMapDataBindingSource source = [selectedFolder: [1,2], selectedPages: [1,2,3,4,5]]
        CsiDashboardShowAllCommand command = new CsiDashboardShowAllCommand()

        when: "are given by the user in dashboard"
        dataBinder.bind(command, source)

        then: "A warning is provided about longer processing time"
        command.shouldWarnAboutLongProcessingTime(
                timeFrameOneYear,
                selectedAggregationIntervallInMintues,
                countOfSelectedBrowser
        ) == true
    }

}
