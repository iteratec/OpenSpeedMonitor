package de.iteratec.osm.batch

/**
 * If an Activity should not be persisted, we pass this dummy to the actual job.
 * This makes sure that the code of the job stays "clean", but we doesn't have the overhead from an activity
 */
class BatchActivityUpdaterDummy extends BatchActivityUpdater {


    BatchActivityUpdaterDummy(String name, String domain, Activity activity, int maximumStages, int saveThreshold) {
        super(name, domain, activity, maximumStages, saveThreshold)
    }
    @Override
    protected void createActivity(name, domain, activity, maximumStages) {
    }

    @Override
    public BatchActivityUpdater beginNewStage(String stageDescription, int maximumStepsInStage, int saveThreshold = 1) {
        return this
    }

    @Override
    public BatchActivityUpdater addProgressToStage(int amount = 1){
        return this
    }
    @Override
    public BatchActivityUpdater addFailures(String lastFailureMessage, int failureAmountToAdd = 1) {
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
    protected void update(){
    }

    @Override
    protected void saveUpdate(boolean flush){
    }

    @Override
    protected void setStatus(Status status) {
    }
}
