package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.BrowserConnectivityWeight
import de.iteratec.osm.report.chart.CsiAggregation
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ConnectivityProfileController)
@Build([ConnectivityProfile, CsiAggregation, Job])
@Mock([ConnectivityProfile, CsiAggregation, Job, BrowserConnectivityWeight])
class ConnectivityProfileControllerSpec extends Specification {

    void "setup"() {
        controller.jobDaoService = new JobDaoService()
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
