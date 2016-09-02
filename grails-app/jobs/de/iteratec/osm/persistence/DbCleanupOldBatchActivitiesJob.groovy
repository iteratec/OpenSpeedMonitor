package de.iteratec.osm.persistence

import de.iteratec.osm.ConfigService
import de.iteratec.osm.InMemoryConfigService
import org.joda.time.DateTime

class DbCleanupOldBatchActivitiesJob {

    DbCleanupService dbCleanupService
    ConfigService configService
    InMemoryConfigService inMemoryConfigService

    boolean createBatchActivity = true

    static triggers = {
        /**
         * Each Day at 3:00 am.
         */
        cron(name: 'DbCleanupOldBatchActivities', cronExpression: '0 0 3 ? * *')
    }

    def execute() {
        if(inMemoryConfigService.isDatabaseCleanupEnabled()){
            Date toDeleteBatchActivitiesBefore = new DateTime().minusDays(configService.getMaxBatchActivityStorageTimeInDays()).toDate()
            dbCleanupService.deletBatchActivityDataBefore(toDeleteBatchActivitiesBefore, createBatchActivity)
        }
    }
}
