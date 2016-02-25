package de.iteratec.osm.batch

import javax.annotation.PreDestroy
import java.text.DecimalFormat

/**
 * Created by benjamin on 04.03.15.
 */
class BatchActivityService implements Observer {

    static transactional = false

    Timer timer = new Timer()
    final Set<BatchActivity> activities = Collections.synchronizedSet(new HashSet<BatchActivity>())
    //Interval to save incoming updates in seconds
    int updateInterval = 5

    BatchActivityService() {
        timer.schedule(new TimerTask() {
            @Override
            void run() {
                updateActivities()
            }
        }, 10000, 1000 * updateInterval)
    }


    /**
     * Creates a new BatchActivity. This BatchActivity will be observed and will automatically be saved, if any property has changed
     * @param c Class of the affected Domain
     * @param idWithinDomain affected object id, will be used to identify already existing activities
     * @param activity running Activity
     * @param name a readable name to display
     * @param observe if true(default) the created activity will be observed and saved
     * @return
     */
    public BatchActivity getActiveBatchActivity(Class c, long idWithinDomain, Activity activity, String name, boolean observe = true) {
        BatchActivity batchActivity
        BatchActivity.withTransaction {
            batchActivity = new BatchActivity(
                    activity: activity,
                    domain: c.toString(),
                    idWithinDomain: idWithinDomain,
                    name: name,
                    failures: 0,
                    lastFailureMessage: "",
                    progress: 0,
                    progressWithinStage: "0",
                    stage: "0",
                    status: Status.ACTIVE,
                    startDate: new Date(),
                    successfulActions: 0).save(flush: true)
            if(observe){
                batchActivity.addObserver(this)
//                batchActivity.save(failOnError: true, flush: true)
            }
        }
        return batchActivity
    }

    /**
     * Checks if there is a running batch for the given Class and ID
     * @param c Class of the affected domain
     * @param idWithinDomain affected object id
     * @return
     */
    public boolean runningBatch(Class c, long idWithinDomain) {
        return (BatchActivity.findByDomainAndIdWithinDomainAndStatus(c.toString(), idWithinDomain, Status.ACTIVE) != null)
    }

    /**
     * Creates a String representation for BatchActivity progress
     * @param count Maximum amount of Activities
     * @param actual activities which are already done
     * @return formatted string
     */
    public String calculateProgress(int count, int actual) {
        DecimalFormat df = new DecimalFormat("#.##");
        if (count == 0) return df.format(0) + " %"
        return df.format(100.0 / count * actual) + " %";
    }

    /**
     * Saves all queued BatchActivities at the moment of the call
     */
    void updateActivities() {
        Set<BatchActivity> activityTemp = []
        //We want to avoid a lost of an Activity, so we need to assure
        //that we take a snapshot of the current set state and clear it, without another thread adding an activity between these two calls
        synchronized (activities) {
            activityTemp.addAll(activities)
            activities.clear()
        }
        if(activityTemp.size()>0){
            BatchActivity.withNewTransaction {
                activityTemp*.save(flush: true)
            }
        }
    }

    /**
     * Makes a note to save the BatchActivity after the next interval
     * @param activity BatchActivity to be saved
     */
    void noteBatchActivityUpdate(BatchActivity activity){
        activities.add(activity)
    }

    /**
     * If the caller passes a BatchActivity, it will be noted to be saved
     * @param o
     * @param arg BatchActivity
     */
    @Override
    void update(Observable o, Object arg) {
        if (arg instanceof BatchActivity) noteBatchActivityUpdate(arg)
    }
}
