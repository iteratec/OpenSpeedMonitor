package de.iteratec.osm.measurement.script

import de.iteratec.osm.csi.Page
import org.grails.databinding.BindUsing

class ArchivedScript {
    /* Default (injected) attributes of GORM */
    Long	id
    /* Automatic timestamping of GORM */
    Date   dateCreated
    Date   lastUpdated
    Script script

    String label
    @BindUsing({
        obj, source -> source['description']
    })
    String description
    @BindUsing({
        obj, source -> source['archiveTag']
    })
    String archiveTag
    String navigationScript

    static mapping = {
        navigationScript(type: 'text')
    }

    static constraints = {
        label(blank: false, maxSize: 255)
        description(blank: true, maxSize: 255)
        archiveTag (blank: true, maxSize: 255)

    }
}
