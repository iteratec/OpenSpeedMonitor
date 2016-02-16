package de.iteratec.osm.csi

import de.iteratec.osm.measurement.schedule.JobGroup


class CsiSystem {

    String label

    static hasMany = [jobGroupWeights: JobGroupWeight]
    static mappedBy = [jobGroupWeights: 'csiSystem']

    static constraints = {
        label unique: true, blank: false
    }

    List<JobGroup> getAffectedJobGroups() {
        return jobGroupWeights*.jobGroup
    }
}
