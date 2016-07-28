package de.iteratec.osm.api.dto

import de.iteratec.osm.measurement.schedule.ConnectivityProfile
import de.iteratec.osm.measurement.schedule.Job

/**
 * Created by marko on 14.06.16.
 */
class JobDto {

    long id
    String label
    boolean active

    ConnectivityProfileDto connectivityProfile
    LocationDto location
    JobGroupDto jobGroup


    public static JobDto create(Job job){
        JobDto result = new JobDto()

        result.id = job.id
        result.label = job.label
        result.active = job.active
        result.connectivityProfile = ConnectivityProfileDto.create(job.connectivityProfile)
        result.location = LocationDto.create(job.location)
        result.jobGroup = JobGroupDto.create(job.jobGroup)
        return result
    }
    public static List<JobDto> create(List<Job> jobs){
        List<JobDto> result = []
        jobs.each {
            result.add(create(it))
        }
        return result
    }
}
