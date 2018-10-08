package de.iteratec.osm.measurement.schedule

import de.iteratec.osm.csi.NonTransactionalIntegrationSpec
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.result.ResultSelectionService
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration(applicationClass = openspeedmonitor.Application.class)
@Rollback
class JobGroupServiceIntSpec extends NonTransactionalIntegrationSpec {
    JobGroupService jobGroupService

    JobGroup notActiveButMeasured

    def setup() {
        jobGroupService.resultSelectionService = Stub(ResultSelectionService) {
            query(_, _, _) >> {
                List result = new ArrayList<>()
                result.add(notActiveButMeasured)
                return result
            }
        }
    }

    void "find active and recently measured job groups"() {
        given: "one active but no measured, not active but recently measured and one neither active nor measured"
        JobGroup activeNotMeasured = JobGroup.build(name: "activeNotMeasured")
        notActiveButMeasured = JobGroup.build(name: "notActiveButMeasured")
        JobGroup notActiveNotMeasured = JobGroup.build(name: "notActiveNotMeasured")

        Job activeNotMeasuredJob = Job.build(jobGroup: activeNotMeasured, active: true, executionSchedule: "1/1 * * ? * *")
        Job notActiveButMeasuredJob = Job.build(jobGroup: notActiveButMeasured, active: false)

        JobResult notActiveButMeasuredJobResult = JobResult.build(jobGroupName: notActiveButMeasured.name, date: new Date())

        when: "service is asked for active or recently measured"
        List activeOrRecent = jobGroupService.getAllActiveAndRecentWithResultInformation()

        then: "only active or recently measured are returned"
        activeOrRecent.size() == 2
        activeOrRecent.every {
            it.name != notActiveNotMeasured.name
        }
        notActiveButMeasured.name == activeOrRecent.find {
            it.dateOfLastResults == notActiveButMeasuredJobResult.date.format("yyyy-MM-dd")
        }.name
        activeNotMeasured.name == activeOrRecent.find {
            it.dateOfLastResults != notActiveButMeasuredJobResult.date.format("yyyy-MM-dd")
        }.name
    }
}
