package de.iteratec.osm.result

class ResultSelectionInformationCreationJob {
    ResultSelectionInformationService resultSelectionInformationService
    static boolean isCurrentlyRunning = false

    static triggers = {
        /** Each Day at midnight. */
        cron(name: 'dailyUpdateEventCleanup', cronExpression: '0 0 0 ? * *')
    }

    def execute() {
        if (isCurrentlyRunning) {
            log.info("Quartz controlled creation of ResultSelectionInformation: Job is already running.")
            return
        }

        isCurrentlyRunning = true
        try {
            resultSelectionInformationService.createLatestResultSelectionInformation()
        } catch (Exception e) {
            log.error("Quartz controlled cleanup of CsiAggregationUpdateEvents throws an exception: " + e.getMessage())
        } finally {
            isCurrentlyRunning = false
        }
    }
}
