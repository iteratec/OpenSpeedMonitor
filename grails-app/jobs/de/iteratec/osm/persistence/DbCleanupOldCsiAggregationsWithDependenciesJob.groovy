package de.iteratec.osm.persistence

import de.iteratec.osm.ConfigService
import de.iteratec.osm.InMemoryConfigService
import org.joda.time.DateTime
import org.quartz.JobExecutionException


class DbCleanupOldCsiAggregationsWithDependenciesJob {

    DbCleanupService dbCleanupService
    ConfigService configService
    InMemoryConfigService inMemoryConfigService

    boolean createBatchActivity = true

    static triggers = {
        /**
         * Each Day at 3:00 am.
         */
        cron(name: 'DailyOldCsiAggregationsWithDependenciesCleanup', cronExpression: '0 0 3 ? * *')
    }

    def execute() {
        if(inMemoryConfigService.isDatabaseCleanupEnabled()) {
            Date toDeleteResultsBefore = new DateTime().minusMonths(configService.getMaxDataStorageTimeInMonths()).toDate()
            dbCleanupService.deleteCsiAggregationsAndCsiAggregationUpdateEventsBefore(toDeleteResultsBefore, createBatchActivity)
        }
    }
}
