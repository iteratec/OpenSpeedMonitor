package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.BrowserConnectivityWeight
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.chart.CsiAggregation
import de.iteratec.osm.result.MeasuredEvent
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

@Build([ConnectivityProfile, CsiAggregation, Job])
class ConnectivityProfileControllerSpec extends Specification implements BuildDataTest,
        ControllerUnitTest<ConnectivityProfileController> {

    void "setup"() {
        controller.jobDaoService = new JobDaoService()
    }

    void setupSpec() {
        mockDomains(ConnectivityProfile, CsiAggregation, Job, BrowserConnectivityWeight, MeasuredEvent, Script)
    }

    void "editing a connectivity profile duplicates and changes it for jobs, but preserves it for existing CsiAggregations"() {
        given: "An existing connectivityProfile, set in job and CSIAggregation"
        ConnectivityProfile connectivityProfile = ConnectivityProfile.build(
                bandwidthDown: 2000,
                bandwidthUp: 100,
                packetLoss: 0,
                latency: 0,
                active: true
        )
        CsiAggregation csiAggregation = CsiAggregation.build(connectivityProfile: connectivityProfile)
        Job job = Job.build(connectivityProfile: connectivityProfile)

        params.id = connectivityProfile.id
        params.name = connectivityProfile.name
        params.active = true
        params.bandwidthDown = 2000
        params.bandwidthUp = 100
        params.packetLoss = 0
        params.latency = 0

        when: "The bandwidth is changed via controller"
        params.bandwidthDown = 6000
        request.method = 'PUT'
        controller.update()

        then: "Actually another ConnectivityProfile is created and changed for existing jobs, but not for CsiAggregations"
        ConnectivityProfile.count == 2
        csiAggregation.connectivityProfile.bandwidthDown == 2000
        job.connectivityProfile.bandwidthDown == 6000
        csiAggregation.connectivityProfile != job.connectivityProfile
    }

}
