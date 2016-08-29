package de.iteratec.osm.batch

/**
 * Use cancel or done, if the BatchActivity is not longer needed
 */
class BatchActivityUpdater {

    private BatchActivity batchActivity
    private int threshold
    private int count

    /**
     *
     * @param name
     * @param domain
     * @param activity
     * @param maximumStages Maximum amount of Stages in an Activity
     * @param saveThreshold A threshold after which updates should be persisted.
     * If you have a few long steps set this low. If you expect many short steps you could increase this value
     */
    public BatchActivityUpdater(String name, String domain, Activity activity, int maximumStages, int saveThreshold = 10){
        this.threshold = saveThreshold
        this.count = count
        createActivity(name,domain,activity,maximumStages)
    }

    protected void createActivity(name, domain, activity, maximumStages) {
        BatchActivity.withNewTransaction {
            this.batchActivity = new BatchActivity(name: name, domain: domain, activity: activity,
                    status: Status.ACTIVE, maximumStages: maximumStages, startDate: new Date(),
                    lastUpdate: new Date()).save(flush: true, failOnError: true)
        }
    }

    /**
     * Ends the current Stage and begins a new one. The BatchActivity will be updated and saved.
     * @param stageDescription String to describe the stage
     * @param maximumStepsInStage Number of steps which have to be done in this stage
     * @param saveThreshold Set a threshold, so te domain will only be saved after the given updates.
     * This will be default set to the previous threshold from beginNewStage or the constructor
     * @return
     */
    public BatchActivityUpdater beginNewStage(String stageDescription, int maximumStepsInStage, int saveThreshold = this.threshold) {
        this.threshold = saveThreshold
        this.count = 0
        batchActivity.actualStage++
        batchActivity.stageDescription = stageDescription
        batchActivity.maximumStepsInStage = maximumStepsInStage
        batchActivity.stepInStage = 0
        saveUpdate(true)
        return this
    }

    public BatchActivityUpdater addProgressToStage(int amount = 1){
        if (amount <= 0){
            throw new Exception("Amount must be > 0")
        }
        batchActivity.stepInStage += amount
        update(amount)
        return this
    }

    public BatchActivityUpdater addFailures(String lastFailureMessage, int failureAmountToAdd = 1) {
        if (failureAmountToAdd <= 0){
            throw new Exception("Amount must be > 0")
        }
        batchActivity.failures += failureAmountToAdd
        batchActivity.lastFailureMessage = lastFailureMessage
        update(failureAmountToAdd)
        return this
    }

    /**
     * Set the status of cancelled and adds an end date to the activity.
     * @return
     */
    public BatchActivityUpdater cancel(){
        setStatus(Status.CANCELLED)
        batchActivity.endDate = new Date()
        saveUpdate(true)
        return this
    }

    /**
     * Set the status of done and adds an end date to the activity.
     * @return
     */
    public BatchActivityUpdater done(){
        setStatus(Status.DONE)
        batchActivity.endDate = new Date()
        saveUpdate(true)
        return this
    }

    /**
     * Saves the underlying BatchActivity, with respect of the threshold.
     */
    protected void update(int updates = 1){
        count += updates
        if(count>= threshold){
            count = 0
            saveUpdate(false)
        }
    }

    /**
     * Sets a new Date to lastUpdate of the BatchActivity and saves it
     */
    protected void saveUpdate(boolean flush){
        BatchActivity.withTransaction {
            batchActivity.lastUpdate = new Date()
            batchActivity.save(flush: flush)
        }
    }

    protected void setStatus(Status status) {
        batchActivity.status = status
    }
}
