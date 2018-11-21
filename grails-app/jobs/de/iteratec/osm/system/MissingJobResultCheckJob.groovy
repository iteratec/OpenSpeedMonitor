package de.iteratec.osm.system

class MissingJobResultCheckJob {

    public static final int FREQUENCY_IN_HOURS = 2
    MissingJobResultCheckService missingJobResultCheckService

    static triggers = {
        cron(name: 'missingJobResultCheckJob', cronExpression: "0 0 */$FREQUENCY_IN_HOURS ? * *")
    }

    def execute() {
        missingJobResultCheckService.fillMissingJobResults()
    }
}
