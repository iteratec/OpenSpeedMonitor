package de.iteratec.osm.d3Data

import org.joda.time.DateTime

/**
 * Created by mmi on 15.10.2015.
 */
class ScheduleChartData {
    String name
    List<ScheduleChartLocation> locations

    List<String> discountedJobs
    String discountedJobsLabel

    List<String> discountedLocations
    String discountedLocationsLabel

    DateTime startDate
    DateTime endDate

    ScheduleChartData() {
        name = "Server"
        locations = new ArrayList<>()

        discountedJobs = new ArrayList<>()
        discountedLocations = new ArrayList<>()

        discountedLocationsLabel = "Missing Label Text"
        discountedJobsLabel = "Missing Label Text"

        startDate = new DateTime()
        endDate = new DateTime()
    }

    def addLocation(ScheduleChartLocation location) {
        locations.add(location)
    }

    def addDiscountedJob(String discription) {
        discountedJobs.add(discription)
    }

    def addDiscountedLocation(String discription) {
        discountedLocations.add(discription)
    }
}
