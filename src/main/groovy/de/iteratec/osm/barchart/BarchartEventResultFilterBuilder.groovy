package de.iteratec.osm.barchart

import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.Selected
import de.iteratec.osm.result.SelectedType

/**
 * Created by mwg on 08.08.2017.
 */
class BarchartEventResultFilterBuilder {
    Object filter

    BarchartEventResultFilterBuilder(Object filter, minValue, maxValue){
        this.filter = filter
        this.filter.between('fullyLoadedTimeInMillisecs', minValue, maxValue)
    }

    BarchartEventResultFilterBuilder withJobResultDate(Date from, Date to){
        if(from && to){
            filter.between('jobResultDate', from, to)
        }
        return this
    }
    BarchartEventResultFilterBuilder withPage(List<Page> allPages){
        if(allPages){
            filter.in('page', allPages)
        }
        return this
    }
    BarchartEventResultFilterBuilder withJobGroup(List<JobGroup> allJobGroups){
        if(allJobGroups){
            filter.in('jobGroup', allJobGroups)
        }
        return this
    }

    BarchartEventResultFilterBuilder withUserTimings(List<Selected> selectedUserTimings){
        if(selectedUserTimings){
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
