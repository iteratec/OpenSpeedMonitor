package de.iteratec.osm.report.external

class JobHealthReportJob {

    JobHealthReportService jobHealthReportService

    static triggers = {
        /** Every five minutes. */
        cron(name: 'JobHealthReport', cronExpression: '0 */5 * ? * * *')
    }

    def execute() {
        Date date = new Date()
        jobHealthReportService.reportJobHealthStatusToGraphite(date)
    }
}
