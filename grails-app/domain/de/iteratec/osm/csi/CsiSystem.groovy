package de.iteratec.osm.csi

import de.iteratec.osm.measurement.schedule.JobGroup


class CsiSystem {

    String label
    List<JobGroupWeight> jobGroupWeights = []

    static hasMany = [jobGroupWeights: JobGroupWeight]

    static constraints = {
        jobGroupWeights minSize: 2
        label unique: true, blank: false
    }
}
