package de.iteratec.osm.batch

class BatchActivityService {

    /**
     */
    public BatchActivityUpdater getActiveBatchActivity(Class c, Activity activity, String name, int maxStages, boolean observe, int saveThreshold = 10) {
        def result
        BatchActivity.withSession {
            if (observe) {
                result = new BatchActivityUpdater(name, c.name, activity, maxStages, saveThreshold)
            } else {
                result = new BatchActivityUpdaterDummy(name, c.name, activity, maxStages, 1)
            }

        }
        return result
    }
    public BatchActivityUpdater createActiveBatchActivity(Class c, Activity activity, String name, int maxStages, boolean observe, int saveThreshold = 10) {
        def result
        BatchActivity.withSession {
            if (observe) {
                result = new BatchActivityUpdater(name, c.name, activity, maxStages, saveThreshold, true)
            }

        }
        return result
    }
    /**
     */
    public BatchActivityUpdater getActiveBatchActivity(long id) {
        def batchActivityUpdater
        BatchActivity.withSession {
            batchActivityUpdater = new BatchActivityUpdater(id)

        }
        return batchActivityUpdater
    }

    /**
     * Checks if there is a running batch for the given Class and ID
     * @param c Class of the affected domain
     * @param idWithinDomain affected object id
     * @return
     */
    public boolean runningBatch(Class c, String name, Activity activity) {
        def result
        BatchActivity.withNewTransaction {
            result = BatchActivity.findByNameAndDomainAndActivityAndStatus(name, c.name, activity, Status.ACTIVE) != null
        }
        return result
    }

}
