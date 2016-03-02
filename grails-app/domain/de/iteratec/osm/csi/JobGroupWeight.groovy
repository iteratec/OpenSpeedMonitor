package de.iteratec.osm.csi

import de.iteratec.osm.measurement.schedule.JobGroup

class JobGroupWeight {
    CsiSystem csiSystem
    JobGroup jobGroup
    Double weight

    static constraints = {
        jobGroup validator: {return it.csiConfiguration != null}
    }
}
