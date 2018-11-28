package de.iteratec.osm.report.external

import de.iteratec.osm.result.EventResult
import grails.gorm.transactions.Transactional

@Transactional
class GraphiteReportService {

    void report(EventResult result) {
        Map dataMap = [eventResult: result]
        GraphiteReportJob.schedule(new Date(), dataMap)
    }
}
