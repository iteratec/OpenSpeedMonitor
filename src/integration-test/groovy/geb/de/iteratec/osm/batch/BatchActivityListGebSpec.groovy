package geb.de.iteratec.osm.batch

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.Status
import de.iteratec.osm.security.Role
import de.iteratec.osm.security.User
import de.iteratec.osm.security.UserRole
import de.iteratec.osm.util.OsmTestLogin
import geb.CustomUrlGebReportingSpec
import geb.pages.de.iteratec.osm.batch.BatchActivityListPage
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Stepwise

@Integration
@Rollback
@Stepwise
class BatchActivityListGebSpec extends CustomUrlGebReportingSpec implements OsmTestLogin {
    void cleanupSpec() {
        cleanUpData()
    }

    void "showOnlyActive Checkbox Test"() {
        given: "some test data"
        createData()
        and: "an authorized user on batchActivity list view"
        doLogin()
        to BatchActivityListPage

        when: "showOnlyActive checkbox is not checked"
        showOnlyActiveCheckbox.value(false)
        then: "all batchActivities are listed"
        batchActivityTableRows.size() == 4

        when: "showOnlyActive checkbox is not checked"
        showOnlyActiveCheckbox.value(true)
        sleep(200)
        then: "only active batchActivities are listed"
        batchActivityTableRows.size() == 1
    }

    private void createData() {
        BatchActivity.withNewTransaction {
            OsmConfiguration.build()
            createAdminUser()
//            BatchActivity.build(status: Status.ACTIVE, startDate: new Date()-1, endDate: new Date(), maximumStages: 1)
            new BatchActivity(name: "test", domain: "irrelevant domain", activity: Activity.UPDATE,
                    status: Status.ACTIVE, startDate: new Date(), lastUpdate: new Date()).save()
            3.times{
                new BatchActivity(name: "test$it", domain: "irrelevant domain", activity: Activity.UPDATE,
                        status: Status.DONE, maximumStages: 1, startDate: new Date(),
                        lastUpdate: new Date()).save()
            }
        }
    }

    private void cleanUpData() {
        doLogout()
        BatchActivity.withNewTransaction {
            BatchActivity.list()*.delete()
            OsmConfiguration.list()*.delete()
            UserRole.list()*.delete()
            User.list()*.delete()
            Role.list()*.delete()
        }
    }
}
