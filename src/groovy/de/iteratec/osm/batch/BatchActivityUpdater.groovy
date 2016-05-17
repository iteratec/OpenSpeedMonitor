package de.iteratec.osm.batch


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
    final static int DELAY = 1000


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
    private final Map<String, Object> snapshot
    protected Timer timer

    public BatchActivityUpdater(String name, String domain, Activity activity, int maximumStages, int updateInterval){
        this.status = Status.ACTIVE
        this.maximumStages = maximumStages
        this.snapshot = [:]
        createActivity(name, domain, activity, maximumStages)
        createTimer(updateInterval)
    }

    protected void createTimer(int updateInterval){
        this.timer = new Timer()
        timer.schedule(new TimerTask() {
            @Override
            void run() {
                saveUpdate()
            }
        }, DELAY, updateInterval)
    }

    protected void createActivity(name, domain, activity, maximumStages) {
        BatchActivity.withTransaction {
            this.batchActivity = new BatchActivity(name: name, domain: domain, activity: activity, status: Status.ACTIVE, maximumStages: maximumStages, startDate: new Date()).save(flush: true, failOnError: true)
        }
    }


    public BatchActivityUpdater beginNewStage(String stageDescription, int maximumStepsInStage) {
        ++actualStage
        this.stageDescription = stageDescription
        this.maximumStepsInStage = maximumStepsInStage
        this.stepInStage = 0
        markForUpdate(ACTUAL_STAGE)
        markForUpdate(STAGE_DESCRIPTION)
        markForUpdate(MAX_STEPS_IN_STAGE)
        markForUpdate(STEP_IN_STAGE)
        return this
    }

    public BatchActivityUpdater addProgressToStage(int amount = 1){
        if (amount <= 0){
            throw new Exception("Amount must be > 0")
        }
        this.stepInStage += amount
        markForUpdate(STEP_IN_STAGE)
        return this
    }

    public BatchActivityUpdater addFailures(int failures = 1) {
        if (failures <= 0){
            throw new Exception("Amount must be > 0")
        }
        this.failures += failures
        markForUpdate(FAILURES)
        return this
    }

    public BatchActivityUpdater setLastFailureMessage(String lastFailureMessage) {
        this.lastFailureMessage = lastFailureMessage
        markForUpdate(LAST_FAILURE_MESSAGE)
        return this
    }

    public BatchActivityUpdater cancel(){
        setStatus(Status.CANCELLED)
        finish()
        return this
    }

    public BatchActivityUpdater done(){
        setStatus(Status.DONE)
        endDate = new Date()
        markForUpdate(END_DATE)
        markForUpdate(STATUS)
        finish()
        return this
    }

    public void update(){
        this.lastUpdate = new Date()
        markForUpdate(LAST_UPDATED)

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


    private void saveUpdate(){
        if(!snapshot.isEmpty()) {
            synchronized (snapshot) {
                snapshot.each { key, value ->
                    batchActivity."$key" = value
                }
                snapshot.clear()
            }
            BatchActivity.withTransaction {
                batchActivity.save(flush: true)
            }
        }
    }

    private void setStatus(Status status) {
        this.status = status
        markForUpdate(STATUS)
    }

    private void finish(){
        timer.cancel()
        update()
        saveUpdate()
    }

    private void markForUpdate(String propertyName){
//        updatedValues[propertyName] = this."$propertyName"
    }
}
