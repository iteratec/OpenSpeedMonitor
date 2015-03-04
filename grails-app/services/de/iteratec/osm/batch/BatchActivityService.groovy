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
                errors: 0,
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

}
