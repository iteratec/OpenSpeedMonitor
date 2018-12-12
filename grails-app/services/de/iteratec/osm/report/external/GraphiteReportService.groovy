package de.iteratec.osm.report.external

import grails.gorm.transactions.Transactional

@Transactional
class GraphiteReportService {

    void report(long jobId, String testid) {
        Map dataMap = [jobId: jobId, 'testId': testid]
        GraphiteReportJob.schedule(new Date(), dataMap)
    }
}
