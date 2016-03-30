package de.iteratec.osm.api.dto

import de.iteratec.osm.measurement.schedule.JobGroup


class JobGroupDto {

    long id

    String name

    CsiConfigurationDto csiConfiguration

    Collection<GraphiteServerDto> graphiteServers= []

    public static JobGroupDto create(JobGroup jobGroup) {
        JobGroupDto result = new JobGroupDto()

        result.id = jobGroup.id
        result.name = jobGroup.name
        if(jobGroup.csiConfiguration != null) {
            result.csiConfiguration = CsiConfigurationDto.create(jobGroup.csiConfiguration)
        }
        result.graphiteServers = GraphiteServerDto.create(jobGroup.graphiteServers)

        return result
    }
    public static Collection<JobGroupDto> create(Collection<JobGroup> jobGroups) {
        Set<JobGroupDto> result = []

        jobGroups.each {
            result.add(create(it))
        }

        return result
    }
}
