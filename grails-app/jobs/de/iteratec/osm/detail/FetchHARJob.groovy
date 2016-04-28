package de.iteratec.osm.detail

import de.iteratec.osm.result.detail.HarFetchService

/**
 * This Job will continuously trigger the fetching of har data, which have been marked to be collected.
 */
class FetchHARJob {
    HarFetchService harFetchService
    /** We use this boolean to make sure, that only one fetching job is running at a time **/
    boolean fetching = false

    static triggers = {
        /**
         * Every minute.
         */
        cron(name: 'FetchHAR', cronExpression: '0 0/1 * ? * *')
    }

    def execute() {
//        if(harFetchService.configService.isDetailFetchingEnabled() && !fetching){
//            fetching = true
//            harFetchService.fetch()
//            fetching = false
//        }
    }
}
