package de.iteratec.osm.report.external

import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.schedule.JobStatistic
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.report.external.provider.GraphiteSocketProvider
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class JobHealthReportServiceSpec extends Specification implements ServiceUnitTest<JobHealthReportService> {

    GraphiteSocket graphiteSocketMock

    def setup() {
        graphiteSocketMock = Mock(GraphiteSocket)
        service.graphiteSocketProvider = Stub(GraphiteSocketProvider) {
            getSocket(_) >> graphiteSocketMock
        }
    }

    void "test reporting of job health"() {
        given:
        GraphiteServer graphiteServer = new GraphiteServer(serverAdress: "test-gserver")
        JobGroup jobGroup = new JobGroup(name: "test-jg", jobHealthGraphiteServers: [graphiteServer])
        Browser browser = new Browser(name: "test-browser")
        Script script = new Script(label: "test-script")
        Location location = new Location(location: "test-location", browser: browser)
        JobStatistic jobStatistic = new JobStatistic(percentageSuccessfulTestsOfLast5: 1.0d, percentageSuccessfulTestsOfLast25: 2.0d, percentageSuccessfulTestsOfLast150: 3.0d)
        JobStatistic jobStatistic2 = new JobStatistic(percentageSuccessfulTestsOfLast5: 1.0d, percentageSuccessfulTestsOfLast25: 2.0d, percentageSuccessfulTestsOfLast150: 3.0d)
        Job job = new Job(label: "test-job1", jobGroup: jobGroup, location: location, script: script, jobStatistic: jobStatistic)
        Job job2 = new Job(label: "test-job2", jobGroup: jobGroup, location: location, script: script, jobStatistic: jobStatistic2)
        Date date = new Date()

        List<Job> jobsToReport = [job, job2]

        when:
        service.reportJobHealthStatusToGraphite(date, jobsToReport)

        then:
        2 * graphiteSocketMock.sendDate( {it.toString().endsWith(".percentageSuccessfulTestsOfLast5")}, 1.0, date)
        2 * graphiteSocketMock.sendDate( {it.toString().endsWith(".percentageSuccessfulTestsOfLast25")}, 2.0, date)
        2 * graphiteSocketMock.sendDate( {it.toString().endsWith(".percentageSuccessfulTestsOfLast150")}, 3.0, date)
    }
}
