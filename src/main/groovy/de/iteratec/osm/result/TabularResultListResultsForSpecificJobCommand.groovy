package de.iteratec.osm.result

import de.iteratec.osm.measurement.schedule.Job

/**
 * Created by msk on 04.04.2016.
 */
public class TabularResultListResultsForSpecificJobCommand extends TabularResultEventResultsCommandBase {
    Job job

    static constraints = { job(nullable: false) }

    @Override
    public void copyRequestDataToViewModelMap(Map<String, Object> viewModelToCopyTo)
    {
        super.copyRequestDataToViewModelMap(viewModelToCopyTo)
        viewModelToCopyTo.put('job', this.job)
    }
}

