package de.iteratec.osm.result

import de.iteratec.osm.report.chart.RepresentableWptResult
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

    GraphLabel(RepresentableWptResult wptResult, Long millisStartOfInterval, SelectedMeasurand selectedMeasurand){
        this.connectivity = getConnectivityName(wptResult)
        this.millisStartOfInterval = millisStartOfInterval
        this.selectedMeasurand = selectedMeasurand
        this.jobGroupId = wptResult.jobGroupId
        this.measuredEventId = wptResult.measuredEventId
        this.pageId = wptResult.pageId
        this.browserId = wptResult.browserId
        this.locationId = wptResult.locationId
        this.tag = "${wptResult.jobGroupId};${wptResult.measuredEventId};${wptResult.pageId};${wptResult.browserId};${wptResult.locationId}"
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

    private static getConnectivityName(RepresentableWptResult wptResult) {
        if (wptResult.connectivityProfile) {
            return wptResult.connectivityProfile
        } else if (wptResult.customConnectivityName) {
            return wptResult.customConnectivityName
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
