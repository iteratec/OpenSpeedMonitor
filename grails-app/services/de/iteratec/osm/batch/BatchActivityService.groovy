package de.iteratec.osm.batch

/**
 * Created by benjamin on 04.03.15.
 */
class BatchActivityService {

    /**
     * Creates a new BatchActivity
     * @param c Class of the affected Domain
     * @param idWithinDomain affected object id
     * @param activity running Activity
     * @param name a readable name to display
     * @return
     */
    public BatchActivity getActiveBatchActivity(Class c,long idWithinDomain, Activity activity,String name){
        return new BatchActivity(
            activity: activity,
                domain: c.toString(),
                idWithinDomain: idWithinDomain,
                name: name,
                failures: 0,
                lastFailureMessage: "",
                progress: 0,
                progressWithinStage: "",
                stage: "",
                status: Status.active,
                startDate: new Date(),
                successfulActions: 0,
        )
    }

    /**
     * Checks if there is a running batch for the given Class and ID
     * @param c Class of the affected domain
     * @param idWithinDomain affected object id
     * @return
     */
    public boolean runningBatch(Class c,long idWithinDomain){
        return (BatchActivity.findByDomainAndIdWithinDomainAndStatus(c.toString(), idWithinDomain, Status.active) != null)
    }

    /**
     *  Updates a BatchActivity with values from the given map and saves it
     *
     * @param BatchActivity BatchActivity to update
     * @param map with following possible entries:
     *      "errors": Integer,
     *      "failures": Integer,
     *      "lastFailureMessage": String,
     *      "progress": Integer,
     *      "progressWithinStage": String,
     *      "stage": String,
     *      "status": Status,
     *      "successfulActions": Integer,
     *      "endDate": Date
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
        activity.save(flush: true)
    }

}
