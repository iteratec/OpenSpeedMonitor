package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.AggregatorType
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.report.chart.MeasuredValueInterval
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.mock.interceptor.MockFor
import org.jsoup.helper.DataUtil
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@TestFor(ConnectivityProfileController)
@Mock([ConnectivityProfileService, ConnectivityProfile, CsiAggregation, Job, MeasuredValueInterval, AggregatorType, Script, Location, JobGroup])
class ConnectivityProfileControllerSpec extends Specification {

    ConnectivityProfileController controllerUnderTest
    ConnectivityProfile existingConnectivityProfile

    CsiAggregation mvWithOldConnectiviyProfile
    Job jobWithOldConnectivityProfile


    void "setup"() {
        controllerUnderTest = controller
        existingConnectivityProfile = new ConnectivityProfile(name: "ConnectivityProfile1", active: true, bandwidthDown: 2000, bandwidthUp: 100, packetLoss: 0, latency: 0).save(failOnError: true)

        existingConnectivityProfile.metaClass.toString = { -> "unused" }

        TestDataUtil.createMeasuredValueIntervals()
        TestDataUtil.createAggregatorTypes()
        mvWithOldConnectiviyProfile = TestDataUtil.createMeasuredValue(new Date(), MeasuredValueInterval.get(1), AggregatorType.get(1), "",1, "", true, existingConnectivityProfile)
        jobWithOldConnectivityProfile = TestDataUtil.createJob("existingJob", new Script(), new Location(), new JobGroup(), "description", 1, false, 50, existingConnectivityProfile)
    }

    // necessary because old measuredValues refer to the connectivityProfile
    void "test editConnectivityProfile creates copy of profile instead of editing the source"() {
        given:
        params.id = existingConnectivityProfile.id
        params.name = "ConnectivityProfile1"
        params.active = true
        params.bandwidthDown = 2000
        params.bandwidthUp = 100
        params.packetLoss = 0
        params.latency = 0

        when:
        params.bandwidthDown = 6000
        controller.update()

        then:
        ConnectivityProfile.count == 2
    }

    void "editing a connectivityProfile will update the jobs using that profile" () {
        given:
        params.id = existingConnectivityProfile.id
        params.name = "ConnectivityProfile1"
        params.active = true
        params.bandwidthDown = 2000
        params.bandwidthUp = 100
        params.packetLoss = 0
        params.latency = 0

        when:
        params.bandwidthDown = 6000
        controller.update()

        then:
        mvWithOldConnectiviyProfile.connectivityProfile.bandwidthDown == 2000
        jobWithOldConnectivityProfile.connectivityProfile.bandwidthDown == 6000
        mvWithOldConnectiviyProfile.connectivityProfile != jobWithOldConnectivityProfile.connectivityProfile
    }
}
