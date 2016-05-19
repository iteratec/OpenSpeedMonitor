package de.iteratec.osm.batch

import org.joda.time.DateTime
import org.joda.time.Seconds

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Use this object to create a BatchActivity. All Updates to this object will be persisted to the actual BatchActivity, after calling update()
 * The persistence happens in another thread.
 * Use cancel or done, if the BatchActivity is not longer needed
 */
class BatchActivityUpdater {

    private final static String END_DATE = "endDate"
    private final static String LAST_UPDATED = "lastUpdate"
    private final static String ACTUAL_STAGE = "actualStage"
    private final static String MAX_STEPS_IN_STAGE = "maximumStepsInStage"
    private final static String STEP_IN_STAGE = "stepInStage"
    private final static String FAILURES = "failures"
    private final static String LAST_FAILURE_MESSAGE = "lastFailureMessage"
    private final static String STAGE_DESCRIPTION = "stageDescription"
    private final static String STATUS = "status"

    private Status status
    private String lastFailureMessage
    private Date lastUpdate
    private Date endDate
    private String stageDescription
    private int stepInStage
    private int failures
    private int maximumStages
    private int actualStage
    private int maximumStepsInStage
    private BatchActivity batchActivity
    /**
     * This map is used to create a snapshot. This map will be checked by the timer and will be persisted and cleared, if this map contains
     * Data. Only use synchronized access to this map.
     */
    private final Map<String, Object> snapshot
    private ScheduledExecutorService executor
    private ScheduledFuture future

    /**
     *
     * @param name
     * @param domain
     * @param activity
     * @param maximumStages Maximum amount of Stages in an Activity
     * @param updateInterval Period in which the updates should be persisted
     * @param timeoutInSeconds Maximum after an updates has been made. If this time is exceeded, the updater will be cancelled.
     */
    public BatchActivityUpdater(String name, String domain, Activity activity, int maximumStages, int updateInterval, int timeoutInSeconds = 1800){
        this.status = Status.ACTIVE
        this.maximumStages = maximumStages
        this.snapshot = [:]
        createUpdateThread(updateInterval, timeoutInSeconds, name, domain, activity, maximumStages)
    }

    protected void createUpdateThread(int updateInterval, int timeoutInSeconds, String name, String domain, Activity activity, int maximumStages){
        executor = Executors.newScheduledThreadPool(1);
        BatchActivityUpdater updater = this
        future = executor.scheduleAtFixedRate(new Runnable() {
            DateTime lastSuccessfulUpdate = DateTime.now()
            @Override
            void run() {
                if(updater.batchActivity == null){
                    createActivity(name,domain,activity,maximumStages)
                }

                boolean updated = updater.saveUpdate()
                if(updated){
                    lastSuccessfulUpdate = DateTime.now()
                } else if((Seconds.secondsBetween(lastSuccessfulUpdate, DateTime.now()).seconds >= timeoutInSeconds)){
                    //The last update was to long ago, we will cancel this activity
                    updater.cancel()
                }
            }
        }, 0, updateInterval, TimeUnit.MILLISECONDS)
    }
    protected void createActivity(name, domain, activity, maximumStages) {
        BatchActivity.withTransaction {
            this.batchActivity = new BatchActivity(name: name, domain: domain, activity: activity, status: Status.ACTIVE, maximumStages: maximumStages, startDate: new Date(), lastUpdate: new Date()).save(flush: true, failOnError: true)
        }
    }

    /**
     * Ends the current Stage and begins a new one
     * @param stageDescription String to describe the stage
     * @param maximumStepsInStage Number of steps which have to be done in this stage
     * @return
     */
    public BatchActivityUpdater beginNewStage(String stageDescription, int maximumStepsInStage) {
        ++actualStage
        this.stageDescription = stageDescription
        this.maximumStepsInStage = maximumStepsInStage
        this.stepInStage = 0
        return this
    }

    public BatchActivityUpdater addProgressToStage(int amount = 1){
        if (amount <= 0){
            throw new Exception("Amount must be > 0")
        }
        this.stepInStage += amount
        return this
    }

    public BatchActivityUpdater addFailures(int failures = 1) {
        if (failures <= 0){
            throw new Exception("Amount must be > 0")
        }
        this.failures += failures
        return this
    }

    public BatchActivityUpdater setLastFailureMessage(String lastFailureMessage) {
        this.lastFailureMessage = lastFailureMessage
        return this
    }

    /**
     * Set the status of cancelled and adds an end date to the activity.
     * This will stop the background updating and start a last update. No Updates will be possible after that.
     * @return
     */
    public BatchActivityUpdater cancel(){
        setStatus(Status.CANCELLED)
        endDate = new Date()
        update()
        cleanup()
        saveUpdate()
        return this
    }

    /**
     * Set the status of done and adds an end date to the activity.
     * This will stop the background updating and start a last update. No Updates will be possible after that.
     * @return
     */
    public BatchActivityUpdater done(){
        setStatus(Status.DONE)
        endDate = new Date()
        update()
        cleanup()
        saveUpdate()
        return this
    }

    /**
     * Set a snapshot of the current state. This snapshot will be persisted to the actual domain.
     */
    public void update(){
        this.lastUpdate = new Date()

        synchronized (snapshot){
            snapshot[END_DATE] = endDate
            snapshot[LAST_UPDATED] = lastUpdate
            snapshot[ACTUAL_STAGE] = actualStage
            snapshot[MAX_STEPS_IN_STAGE] = maximumStepsInStage
            snapshot[STEP_IN_STAGE] = stepInStage
            snapshot[FAILURES] = failures
            snapshot[LAST_FAILURE_MESSAGE] = lastFailureMessage
            snapshot[STAGE_DESCRIPTION] = stageDescription
            snapshot[STATUS] = status
        }
    }

    /**
     * Takes the snapshot, synchronizes the underlying BatchActivity with it and saves it.
     * Returns if an updates has been made or not
     */
    private boolean saveUpdate(){
        boolean notEmpty = false
        synchronized (snapshot) {
            if (!snapshot.isEmpty()) {
                notEmpty = true
                snapshotToDomain()
            }
        }
        if(notEmpty){
            BatchActivity.withTransaction {
                batchActivity.save(flush: true).merge()
            }
        }
        if(snapshot."status" == Status.DONE || snapshot."status" == Status.CANCELLED){
            cleanup()
        }

        return notEmpty
    }

    /**
     * Takes the snapshot map and writes it's data to the domain object
     */
    private void snapshotToDomain(){
        snapshot.each { key, value ->
            batchActivity."$key" = value
        }
        snapshot.clear()
    }

    private void setStatus(Status status) {
        this.status = status
    }

    /**
     * Stops the executer and its task. No Updates will be made after this Methods.
     * If a current task is running this method will wait until it's finished or 2 seconds passed.
     */
    private void cleanup(){
        future.cancel(false)
        executor.shutdown()
        executor.awaitTermination(2, TimeUnit.SECONDS)
    }
}
