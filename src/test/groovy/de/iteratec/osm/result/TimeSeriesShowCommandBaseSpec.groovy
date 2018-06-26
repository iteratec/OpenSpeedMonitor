package de.iteratec.osm.result

import grails.databinding.SimpleMapDataBindingSource
import org.grails.testing.GrailsUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Interval
import spock.lang.Specification

class TimeSeriesShowCommandBaseSpec extends Specification implements GrailsUnitTest {

    public static final String CUSTOM_CONNECTIVITY_NAME = 'Custom (6.000/512 Kbps, 50ms)'
    TimeSeriesShowCommandBase command
    def dataBinder

    void setup() {
        dataBinder = applicationContext.getBean('grailsWebDataBinder')
        command = new TimeSeriesShowCommandBase()
    }

    void "command without bound parameters is invalid"() {
        expect:
        !command.validate()
        command.selectedFolder == []
        command.selectedPages == []
        command.selectedMeasuredEventIds == []
        command.selectedBrowsers == []
        command.selectedLocations == []
    }

    void "command with empty bound parameters is invalid"() {
        when:
        dataBinder.bind(command, [:] as SimpleMapDataBindingSource)

        then:
        !command.validate()
        command.selectedFolder == []
        command.selectedPages == []
        command.selectedMeasuredEventIds == []
        command.selectedBrowsers == []
        command.selectedLocations == []
    }

    void "command bound with default parameters is valid"() {
        given:
        Map params = getDefaultParams()

        when:
        dataBinder.bind(command, params as SimpleMapDataBindingSource)

        then:
        command.validate()
        command.from == new DateTime(2013, 8, 18, 16, 0, 0, DateTimeZone.UTC)
        command.to == new DateTime(2013, 8, 18, 18, 0, 0, DateTimeZone.UTC)
        command.createTimeFrameInterval().start == command.from
        command.createTimeFrameInterval().end == command.to
        command.selectedFolder == [1L]
        command.selectedPages == [1L, 5L]
        command.selectedMeasuredEventIds == [7L, 8L, 9L]
        command.selectedBrowsers == [2L]
        command.selectedLocations == [17L]
        command.includeNativeConnectivity
        command.getSelectedCustomConnectivityNames() == [CUSTOM_CONNECTIVITY_NAME]
        command.selectedConnectivityProfiles == [1L]
    }

    void "command without browsers, locations, connectivities and measuredEvents is valid"() {
        given:
        Map params = getDefaultParams()
        params.remove("selectedMeasuredEventIds")
        params.remove("selectedConnectivities")
        params.remove("selectedBrowsers")
        params.remove("selectedLocations")

        when:
        dataBinder.bind(command, params as SimpleMapDataBindingSource)

        then:
        command.validate()
        command.selectedMeasuredEventIds == []
        command.selectedBrowsers == []
        command.selectedLocations == []
        command.includeNativeConnectivity
        command.getSelectedCustomConnectivityNames() == []
        command.selectedConnectivityProfiles == []
    }

    void "command is invalid if 'to' is before 'from'"() {
        given:
        Map params = getDefaultParams()
        params.from = "2013-08-18T12:00:00.000Z"
        params.to = "2013-08-18T11:00:00.000Z"

        when:
        dataBinder.bind(command, params as SimpleMapDataBindingSource)

        then:
        !command.validate()
    }

    void "command is invalid if 'to' is equal to 'from'"() {
        given:
        Map params = getDefaultParams()
        params.from = "2013-08-18T11:00:00.000Z"
        params.to = "2013-08-18T11:00:00.000Z"

        when:
        dataBinder.bind(command, params as SimpleMapDataBindingSource)

        then:
        !command.validate()
    }

    void "command supports legacy date format in 'from' and 'to'"() {
        given:
        Map params = getDefaultParams()
        params.from = "18.08.2013"
        params.to = "18.08.2013"

        when:
        dataBinder.bind(command, params as SimpleMapDataBindingSource)

        then:
        command.validate()
        command.from == new DateTime(2013, 8, 18, 0, 0, 0, 0)
        command.to == new DateTime(2013, 8, 18, 23, 59, 59, 999)
        command.createTimeFrameInterval().start == command.from
        command.createTimeFrameInterval().end == command.to
    }

    void "command supports automatic time frame"() {
        given:
        Map params = getDefaultParams()
        params.from = null
        params.to = null
        params.selectedTimeFrameInterval = 3000
        long nowInMillis = DateTime.now().getMillis()
        long allowedDelta = 1000

        when:
        dataBinder.bind(command, params as SimpleMapDataBindingSource)

        then:
        command.validate()
        command.from == null
        command.to == null
        Interval timeFrame = command.createTimeFrameInterval()
        Math.abs(timeFrame.endMillis - nowInMillis) < allowedDelta
        Math.abs(timeFrame.startMillis - (nowInMillis - 3000 * 1000)) < allowedDelta
    }

    void "command is invalid when binding parameters of wrong type"() {
        given:
        Map params = getDefaultParams()
        params.selectedPages = ['NOT-A-NUMBER']
        params.selectedLocations = 'UGLY'


        when:
        dataBinder.bind(command, params as SimpleMapDataBindingSource)

        then:
        !command.validate()
        command.selectedPages == []
        command.selectedLocations == []
    }

    void "command is invalid without pages and measured events"() {
        given:
        Map params = getDefaultParams()
        params.selectedPages = []
        params.selectedMeasuredEventIds = []

        when:
        dataBinder.bind(command, params as SimpleMapDataBindingSource)

        then:
        !command.validate()
    }

    void "command is valid without pages but with measured events"() {
        given:
        Map params = getDefaultParams()
        params.selectedPages = []
        params.selectedMeasuredEventIds = ["1", "2"]

        when:
        dataBinder.bind(command, params as SimpleMapDataBindingSource)

        then:
        command.validate()
    }

    void "command does not include native or custom if only numbers are set"() {
        given:
        Map params = getDefaultParams()
        params.selectedConnectivities = ['1', '2']

        when:
        dataBinder.bind(command, params as SimpleMapDataBindingSource)

        then:
        command.validate()
        command.getSelectedCustomConnectivityNames() == []
        !command.includeNativeConnectivity
        command.getSelectedConnectivityProfiles() == [1L, 2L]
    }

    void "command properties are correctly copied to map"() {
        given:
        setDefaultCommandProperties(command)

        when:
        Map<String, Object> result = [:]
        command.copyRequestDataToViewModelMap(result)

        then:
        result.size() == 20
        result["selectedFolder"] == [1L]
        result["selectedPages"] == [1L, 5L]
        result['selectedFolder'] == [1L]
        result['selectedPages'] == [1L, 5L]
        result['selectedMeasuredEventIds'] == [7L, 8L, 9L]
        result['selectedBrowsers'] == [2L]
        result['selectedLocations'] == [17L]

        result['from'] == '2013-08-18T12:00:00.000Z'
        result['to'] == '2013-08-19T13:00:00.000Z'
        result['selectedConnectivities'] == [CUSTOM_CONNECTIVITY_NAME, "1", "native"]
    }

    void "command creates correct ErQueryParameters"() {
        given:
        setDefaultCommandProperties(command)

        when:
        MvQueryParams mvQueryParams = command.createMvQueryParams()

        then:
        mvQueryParams != null
        mvQueryParams.jobGroupIds == [1L] as SortedSet
        mvQueryParams.pageIds == [1L, 5L] as SortedSet
        mvQueryParams.measuredEventIds == [7L, 8L, 9L] as SortedSet
        mvQueryParams.browserIds == [2L] as SortedSet
        mvQueryParams.locationIds == [17L] as SortedSet
        mvQueryParams.connectivityProfileIds == [1L] as SortedSet
        !mvQueryParams.includeAllConnectivities
    }

    void "command creates correct ErQueryParameters with empty filters"() {
        given:
        setDefaultCommandProperties(command)
        command.selectedConnectivities = []
        command.selectedMeasuredEventIds = []
        command.selectedBrowsers = []
        command.selectedLocations = []

        when:
        MvQueryParams mvQueryParams = command.createMvQueryParams()

        then:
        mvQueryParams.measuredEventIds == [] as Set
        mvQueryParams.browserIds == [] as Set
        mvQueryParams.locationIds == [] as Set
        mvQueryParams.connectivityProfileIds == [] as Set
        mvQueryParams.includeAllConnectivities
    }

    void "command creates correct ErQueryParameters with only custom connectivites"() {
        given:
        setDefaultCommandProperties(command)
        command.selectedConnectivities = [CUSTOM_CONNECTIVITY_NAME]

        when:
        MvQueryParams mvQueryParams = command.createMvQueryParams()

        then:
        mvQueryParams.connectivityProfileIds == [] as Set
        !mvQueryParams.includeAllConnectivities
    }

    void "createMvQueryParams throws with invalid command"() {
        given: "an invalid command"
        !command.validate()

        when:
        command.createMvQueryParams()

        then: "an exception is thrown"
        thrown IllegalStateException
    }

    static Map getDefaultParams() {
        return [
            from : '2013-08-18T16:00:00.000Z',
            to : '2013-08-18T18:00:00.000Z',
            selectedFolder : '1',
            selectedPages : ['1', '5'],
            selectedMeasuredEventIds : ['7', '8', '9'],
            selectedConnectivities : ['1', CUSTOM_CONNECTIVITY_NAME, 'native'],
            selectedBrowsers : '2',
            selectedLocations : '17',
            showDataMarkers : false,
            showDataLabels : false,
            selectedTimeFrameInterval : 0,
            selectChartType : 0,
            chartWidth : 0,
            chartHeight : 0,
            loadTimeMinimum : 0
        ]
    }

    static setDefaultCommandProperties(TimeSeriesShowCommandBase command) {
        command.from = new DateTime(2013, 8, 18, 12, 0, 0, DateTimeZone.UTC)
        command.to = new DateTime(2013, 8, 19, 13, 0, 0, DateTimeZone.UTC)
        command.selectedFolder = [1L]
        command.selectedPages = [1L, 5L]
        command.selectedMeasuredEventIds = [7L, 8L, 9L]
        command.selectedBrowsers = [2L]
        command.selectedLocations = [17L]
        command.selectedConnectivities = [CUSTOM_CONNECTIVITY_NAME, '1', 'native']

        command.chartWidth = 0
        command.chartHeight = 0
        command.loadTimeMinimum = 0
        command.showDataMarkers = false
        command.showDataLabels = false
    }

}
