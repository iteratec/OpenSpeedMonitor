package de.iteratec.osm.d3Data

import org.joda.time.DateTime

/**
 * This class represents a model for the creation of a d3 schedule chart
 * A schedule chart is separated into n location.
 * Each location is again separated into m jobs.
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
