import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.MeasuredEvent
import de.iteratec.osm.measurement.environment.WebPageTestServer

testDataConfig {
    sampleData {
        'de.iteratec.osm.measurement.schedule.ConnectivityProfile' {
            def i = 1
            name = {-> "nameIncrementedViaBuildTestDataConfig_${i++}" }
        }
        'de.iteratec.osm.measurement.schedule.Job' {
            def i = 1
            label = {-> "labelIncrementedViaBuildTestDataConfig_${i++}" }
            // build-test-data plugin doesn't understand custom constraints for connectivityProfile in Job class.
            connectivityProfile = { -> ConnectivityProfile.build() }
            script = { -> Script.build() }
        }
        'de.iteratec.osm.result.EventResult' {
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
        'de.iteratec.osm.measurement.environment.Browser' {
            def i = 1
            name = {-> "nameIncrementedViaBuildTestDataConfig_${i++}" }
        }
        'de.iteratec.osm.csi.Page' {
            def i = 1
            name = {-> "nameIncrementedViaBuildTestDataConfig_${i++}" }
        }
        'de.iteratec.osm.csi.Page' {
            def i = 1
            name = {-> "nameIncrementedViaBuildTestDataConfig_${i++}" }
        }
        'de.iteratec.osm.measurement.environment.WebPageTestServer' {
            baseUrl = {-> "http://wptserver/" }
        }
        'de.iteratec.osm.result.MeasuredEvent' {
            def i = 1
            name = {-> "nameIncrementedViaBuildTestDataConfig_${i++}" }
        }
        'de.iteratec.osm.measurement.script.Script' {
            def i = 1
            label = {-> "labelIncrementedViaBuildTestDataConfig_${i++}" }
        }
        'de.iteratec.osm.csi.CsiDay' {
            hour0Weight = {-> 1}
            hour1Weight = {-> 1}
            hour2Weight = {-> 1}
            hour3Weight = {-> 1}
            hour4Weight = {-> 1}
            hour5Weight = {-> 1}
            hour6Weight = {-> 1}
            hour7Weight = {-> 1}
            hour8Weight = {-> 1}
            hour9Weight = {-> 1}
            hour10Weight = {-> 1}
            hour11Weight = {-> 1}
            hour12Weight = {-> 1}
            hour13Weight = {-> 1}
            hour14Weight = {-> 1}
            hour15Weight = {-> 1}
            hour16Weight = {-> 1}
            hour17Weight = {-> 1}
            hour18Weight = {-> 1}
            hour19Weight = {-> 1}
            hour20Weight = {-> 1}
            hour21Weight = {-> 1}
            hour22Weight = {-> 1}
            hour23Weight = {-> 1}
        }
    }
    unitAdditionalBuild = [
            'de.iteratec.osm.measurement.schedule.Job' : [
                    de.iteratec.osm.measurement.schedule.ConnectivityProfile,
                    de.iteratec.osm.measurement.script.Script
            ],
            'de.iteratec.osm.result.EventResult' : [
                    de.iteratec.osm.measurement.schedule.ConnectivityProfile
            ],
            'de.iteratec.osm.report.chart.CsiAggregation': [
                    de.iteratec.osm.measurement.schedule.JobGroup,
                    de.iteratec.osm.result.MeasuredEvent,
                    de.iteratec.osm.csi.Page,
                    de.iteratec.osm.measurement.environment.Browser,
                    de.iteratec.osm.measurement.environment.Location
            ]
    ]
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
