import de.iteratec.osm.measurement.schedule.ConnectivityProfile

testDataConfig {
    sampleData {
        'de.iteratec.osm.measurement.schedule.Job' {
            // build-test-data plugin doesn't understand custom constraints for connectivityProfile in Job class.
            connectivityProfile = { -> ConnectivityProfile.build() }
        }
        'de.iteratec.osm.csi.CsiConfiguration' {
            def i = 1
            label = {-> "labelIncrementedViaBuildTestDataConfig_${i++}" }
        }
    }
    unitAdditionalBuild = ['de.iteratec.osm.measurement.schedule.Job': [de.iteratec.osm.measurement.schedule.ConnectivityProfile]]
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