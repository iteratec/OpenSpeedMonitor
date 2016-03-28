package de.iteratec.osm.persistence

import de.iteratec.osm.ConfigService
import de.iteratec.osm.InMemoryConfigService
import org.joda.time.DateTime

class DbCleanupOldHarJob {

    DbCleanupService dbCleanupService
    ConfigService configService
    InMemoryConfigService inMemoryConfigService

    boolean createBatchActivity = true

    static triggers = {
        /**
         * Each Day at 3:00 am.
         */
        cron(name: 'DailyOldHarCleanup', cronExpression: '0 0 3 ? * *')
    }

    def execute() {
        if(configService.isDetailFetchingEnabled()&& inMemoryConfigService.isDatabaseCleanupEnabled()){
            //TODO add an extra variable to declare time to persist har data
            Date toDeleteResultsBefore = new DateTime().minusMonths(configService.getMaxDataStorageTimeInMonths()).toDate()
            dbCleanupService.deleteHarDataBefore(toDeleteResultsBefore, createBatchActivity)
        }
    }
}
