package de.iteratec.osm.api.dto

import de.iteratec.osm.measurement.schedule.JobGroup


class JsonJobGroup {

    long id

    String name

    JsonCsiConfiguration csiConfiguration

    Collection<JsonGraphiteServer> graphiteServers= []

    public static JsonJobGroup create(JobGroup jobGroup) {
        JsonJobGroup result = new JsonJobGroup()

        result.id = jobGroup.id
        result.name = jobGroup.name
        if(jobGroup.csiConfiguration != null) {
            result.csiConfiguration = JsonCsiConfiguration.create(jobGroup.csiConfiguration)
        }
        result.graphiteServers = JsonGraphiteServer.create(jobGroup.graphiteServers)

        return result
    }
    public static Set<JsonJobGroup> create(Set<JobGroup> jobGroups) {
        Set<JsonJobGroup> result = []

        jobGroups.each {
            result.add(create(it))
        }

        return result
    }
}
