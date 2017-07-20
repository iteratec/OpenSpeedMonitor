package de.iteratec.osm.result

import groovy.transform.EqualsAndHashCode
import groovy.transform.InheritConstructors

import static de.iteratec.osm.util.Constants.UNIQUE_STRING_DELIMITTER

/**
 * Created by mwg on 18.07.2017.
 */
@EqualsAndHashCode(excludes = "measuredEventId")
@InheritConstructors
class GraphLabel  implements Comparable<GraphLabel> {
    SelectedMeasurand selectedMeasurand
    Long jobGroupId, measuredEventId, pageId, browserId, locationId
    Long millisStartOfInterval
    String connectivity, tag

    GraphLabel(EventResult eventResult, Long millisStartOfInterval, SelectedMeasurand selectedMeasurand){
        this.connectivity = getConnectivityName(eventResult)
        this.millisStartOfInterval = millisStartOfInterval
        this.selectedMeasurand = selectedMeasurand
        this.jobGroupId = eventResult.jobGroupId
        this.measuredEventId = eventResult.measuredEventId
        this.pageId = eventResult.pageId
        this.browserId = eventResult.browserId
        this.locationId = eventResult.locationId
        this.tag = "${eventResult.jobGroupId};${eventResult.measuredEventId};${eventResult.pageId};${eventResult.browserId};${eventResult.locationId}"
    }

    GraphLabel createCopy(boolean  withMilliseconds){
        GraphLabel result = new GraphLabel(
                selectedMeasurand: this.selectedMeasurand,
                jobGroupId: this.jobGroupId,
                measuredEventId: this.measuredEventId,
                pageId:  this.pageId,
                browserId: this.browserId,
                locationId:  this.locationId,
                connectivity: this.connectivity,
                tag:  this.tag
        )

        if(withMilliseconds){
            result.millisStartOfInterval = this.millisStartOfInterval
        }

        return result
    }

    void validate(){
        this.properties.each{ property, value ->
            if(property != "millisStartOfInterval" && value == null){
                throw new IllegalArgumentException("Validation failed: ${property} is null.")
            }
        }
    }

    private static getConnectivityName(EventResult eventResult) {
        if (eventResult.connectivityProfile) {
            return eventResult.connectivityProfile.name
        } else if (eventResult.customConnectivityName) {
            return eventResult.customConnectivityName
        } else {
            return "native"
        }
    }


    @Override
    String toString(){
        if(!millisStartOfInterval){
            return selectedMeasurand.toString()+UNIQUE_STRING_DELIMITTER+tag+UNIQUE_STRING_DELIMITTER+connectivity
        }else {
            return selectedMeasurand.toString()+UNIQUE_STRING_DELIMITTER+tag+UNIQUE_STRING_DELIMITTER+millisStartOfInterval+UNIQUE_STRING_DELIMITTER+connectivity
        }
    }

    @Override
    int compareTo(GraphLabel graphLabel) {
        if(graphLabel){
            return this.toString().compareTo(graphLabel.toString())
        }else{
            return 1
        }
    }
}
