package de.iteratec.osm.batch

/**
 * If an Activity should not be persisted, we pass this dummy to the actual job.
 * This makes sure that the code of the job stays "clean", but we doesn't have the overhead from an activity
 */
class BatchActivityUpdaterDummy extends BatchActivityUpdater {


    BatchActivityUpdaterDummy(String name, String domain, Activity activity, int maximumStages, int updateInterval) {
        super(name, domain, activity, maximumStages, updateInterval)
    }

    @Override
    protected void createTimer(int updateInterval, int timeoutInSeconds){
        //do nothing
    }
    @Override
    protected void createActivity(name, domain, activity, maximumStages) {
        //do nothing
    }

    @Override
    public BatchActivityUpdater beginNewStage(String stageDescription, int maximumStepsInStage) {
        return this
    }

    @Override
    public BatchActivityUpdater addProgressToStage(int amount = 1){
        return this
    }
    @Override
    public BatchActivityUpdater addFailures(int failures = 1) {
        return this
    }
    @Override
    public BatchActivityUpdater setLastFailureMessage(String lastFailureMessage) {
        return this
    }
    @Override
    public BatchActivityUpdater cancel(){
        return this
    }
    @Override
    public BatchActivityUpdater done(){
        return this
    }
    @Override
    public void update(){
        //do nothing
    }
}
