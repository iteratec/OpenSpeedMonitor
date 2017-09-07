package de.iteratec.osm.result

import de.iteratec.osm.measurement.schedule.Job

/**
 * <p>

 * A threshold contains the thresholds of a {@Link EventResult}.
 * After the {@link Page} is loaded measurement values can be compared with the threshold values.
 * Every Threshold class has a metric and two threshold values. One Value
 * is the upper boundary and the other one the lower boundary. If the value is above
 * the upper boundary the measurement is bad. If the measurement value is between the values
 * the value is ok. And at least if the value is under the lower boundary the measurement is good.
 * The Threshold is assigned to a {@Link Job} and to a {@Link MeasuredEvent}.
 * </p>
 *
 * @author owe
 */
class Threshold {

    /**
     * A Threshold belongs to a job and a measured event.
     */
    Job job
    MeasuredEvent measuredEvent
    static belongsTo = [job: Job, measuredEvent: MeasuredEvent]

    /**
     * The measurand of the threshold
     */
    Measurand measurand

    /**
     * The upper boundary.
     * Values above this value are 'bad'.
     */
    Integer upperBoundary

    /**
     * The lower boundary.
     * Values under this value are 'good'.
     */
    Integer lowerBoundary

    /**
     * Set the constraints
     */
    static constraints = {
        measurand(nullable: false)
        upperBoundary(nullable: false, validator: {currentUpperBoundary, thresholdInstance ->
           return  thresholdInstance.lowerBoundary == null || currentUpperBoundary > thresholdInstance.lowerBoundary
        })
        lowerBoundary(nullable: false, validator: {currentLowerBoundary, thresholdInstance ->
            return currentLowerBoundary >= 0 && (thresholdInstance.upperBoundary == null || currentLowerBoundary < thresholdInstance.upperBoundary)
        })
    }
}
