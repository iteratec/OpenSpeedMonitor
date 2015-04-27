package de.iteratec.osm.batch

import java.text.DecimalFormat

/**
 * Created by benjamin on 04.03.15.
 */
class BatchActivityService {

    static transactional = false

    /**
     * Creates a new BatchActivity
     * @param c Class of the affected Domain
     * @param idWithinDomain affected object id
     * @param activity running Activity
     * @param name a readable name to display
     * @return
     */
    public BatchActivity getActiveBatchActivity(Class c,long idWithinDomain, Activity activity,String name){
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
                    successfulActions: 0,
            ).save(failOnError: true, flush: true)
        }
        return batchActivity
    }

    /**
     * Checks if there is a running batch for the given Class and ID
     * @param c Class of the affected domain
     * @param idWithinDomain affected object id
     * @return
     */
    public boolean runningBatch(Class c,long idWithinDomain){
        return (BatchActivity.findByDomainAndIdWithinDomainAndStatus(c.toString(), idWithinDomain, Status.ACTIVE) != null)
    }

    /**
     *  Updates a BatchActivity with values from the given map and saves it
     *
     * @param BatchActivity BatchActivity to update
     * @param map with following possible entries:
     *      <li>"errors": Integer,</li>
     *      <li>"failures": Integer,</li>
     *      <li>"lastFailureMessage": String,</li>
     *      <li>"progress": String,</li>
     *      <li>"progressWithinStage": String,</li>
     *      <li>"stage": String,</li>
     *      <li>"status": Status,</li>
     *      <li>"successfulActions": Integer,</li>
     *      <li>"endDate": Date</li>
     */
    public void updateStatus(BatchActivity activity,Map<String,Object> map){
        def allowed = ["errors","failures","lastFailureMessage","progress","progressWithinStage","stage","status","successfulActions","endDate"]
        map.each {key,value->
            if(allowed.contains(key)){
                activity[key] = value
            } else{
                log.error("$key not allowed for ${activity.class}")
            }
        }
        BatchActivity.withTransaction {
            activity.save(flush: true)
        }
    }


    /**
     * Creates a String representation for BatchActivity progress
     * @param count Maximum amount of Activities
     * @param actual activities which are already done
     * @return formatted string
     */
    public String calculateProgress(int count, int actual){
        DecimalFormat df = new DecimalFormat("#.##");
        if (count == 0) return df.format(0)+" %"
        return df.format(100.0/count*actual) + " %";
    }

}
