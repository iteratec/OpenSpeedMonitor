package de.iteratec.osm.batch

class BatchActivityService {



    /**
     */
    public BatchActivityUpdater getActiveBatchActivity(Class c, Activity activity, String name, int maxStages, boolean observe) {
        if(observe){
            return new BatchActivityUpdater(name,c.name,activity, maxStages, 5000)
        } else{
            return new BatchActivityUpdaterDummy(name,c.name,activity, maxStages, 5000)
        }
    }

    /**
     * Checks if there is a running batch for the given Class and ID
     * @param c Class of the affected domain
     * @param idWithinDomain affected object id
     * @return
     */
    public boolean runningBatch(Class c, String name, Activity activity) {
        return (BatchActivity.findByNameAndDomainAndActivityAndStatus(name, c.name, activity, Status.ACTIVE) != null)
    }

}
