import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.MeasuredEvent

testDataConfig {
    sampleData {
        'de.iteratec.osm.measurement.schedule.Job' {
            // build-test-data plugin doesn't understand custom constraints for connectivityProfile in Job class.
            connectivityProfile = { -> ConnectivityProfile.build() }
        }
        'de.iteratec.osm.result.EventResult' {
            // build-test-data plugin doesn't understand custom constraints for connectivityProfile in Job class.
            connectivityProfile = { -> ConnectivityProfile.build() }
        }
        'de.iteratec.osm.csi.CsiConfiguration' {
            def i = 1
            label = {-> "labelIncrementedViaBuildTestDataConfig_${i++}" }
        }
        'de.iteratec.osm.measurement.schedule.JobGroup' {
            def i = 1
            name = {-> "nameIncrementedViaBuildTestDataConfig_${i++}" }
            graphiteServers = {-> []}
        }
        'de.iteratec.osm.report.chart.CsiAggregation' {
            jobGroup = { -> JobGroup.build()}
            measuredEvent = { -> MeasuredEvent.build()}
            page = {-> Page.build()}
            browser = {-> Browser.build()}
            location = {->Location.build()}

        }
    }
    unitAdditionalBuild = ['de.iteratec.osm.measurement.schedule.Job': [de.iteratec.osm.measurement.schedule.ConnectivityProfile],
                            'de.iteratec.osm.report.chart.CsiAggregation': [de.iteratec.osm.measurement.schedule.JobGroup,
                                                                            de.iteratec.osm.result.MeasuredEvent,
                                                                            de.iteratec.osm.csi.Page,
                                                                            de.iteratec.osm.measurement.environment.Browser,
                                                                            de.iteratec.osm.measurement.environment.Location]]
}
environments {
    production {
        testDataConfig {
            // The build-test-data plugin should be safe and have no real impact on environments that it's build methods aren't used in.
            // If you'd still like to disable it (to gain a small startup performance benefit by not decorating the metaClass),
            // you can set the enabled flag to false.
            // see https://github.com/longwa/build-test-data/wiki/TestDataConfig#turning-off-the-build-test-data-plugin-in-some-environments/
            enabled = false
        }
    }
}