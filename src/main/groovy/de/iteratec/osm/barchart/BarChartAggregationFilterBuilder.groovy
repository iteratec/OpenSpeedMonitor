package de.iteratec.osm.barchart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.Selected
import de.iteratec.osm.result.SelectedType

/**
 * Created by mwg on 08.08.2017.
 */
class BarChartAggregationFilterBuilder {
    Object filter

    BarChartAggregationFilterBuilder(Object filter, minValue, maxValue){
        this.filter = filter
        this.filter.between('fullyLoadedTimeInMillisecs', minValue, maxValue)
    }

    BarChartAggregationFilterBuilder withJobResultDate(Date from, Date to){
        filter.between('jobResultDate', from, to)
        return this
    }
    BarChartAggregationFilterBuilder withPage(List<Page> allPages){
        if(allPages && allPages.size() >= 1){
            filter.in('page', allPages)
        }
        return this
    }
    BarChartAggregationFilterBuilder withJobGroup(List<JobGroup> allJobGroups){
        if(allJobGroups && allJobGroups.size() >= 1){
            filter.in('jobGroup', allJobGroups)
        }
        return this
    }

    BarChartAggregationFilterBuilder withUserTimings(List<Selected> selectedUserTimings){
        if(selectedUserTimings && selectedUserTimings.size() >= 1){
            List<String> userTimingNames = selectedUserTimings.findAll{it.selectedType != SelectedType.MEASURAND}.collect{it.getDatabaseRelevantName()}
            filter.userTimings {
                or{
                    userTimingNames.each { String name ->
                        eq('name', name)
                    }
                }
            }
        }
        return this
    }

    Object getFilter(){
        return filter
    }
}
