package de.iteratec.osm.batch

import java.text.DecimalFormat



class BatchActivityService {

    static transactional = false


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
        return (BatchActivity.findByNameAndDomainAndActivityAndStatus(name, c.toString(), activity, Status.ACTIVE) != null)
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
}
