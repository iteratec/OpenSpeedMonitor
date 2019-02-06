package de.iteratec.osm.report.external

import grails.util.Environment

class JobHealthReportJob {

    JobHealthReportService jobHealthReportService

    static triggers = {
        /** Every five minutes. */
        cron(name: 'JobHealthReport', cronExpression: '0 */5 * ? * * *')
    }

    def execute() {
        if (Environment.getCurrent() == Environment.PRODUCTION) {
            Date date = new Date()
            jobHealthReportService.reportJobHealthStatusToGraphite(date)
        }
    }
}
