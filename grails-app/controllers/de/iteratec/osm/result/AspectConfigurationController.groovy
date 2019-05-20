package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.util.ControllerUtils
import grails.validation.Validateable
import org.springframework.http.HttpStatus

class AspectConfigurationController {

    def getPage(PageCommand cmd) {
        Page page = Page.get(cmd.pageId)
        if (page) {
            ControllerUtils.sendObjectAsJSON(response, page)
        } else {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "No Page with id '${cmd.pageId}' could be found.")
        }
    }
}
class PageCommand implements Validateable{
    Long pageId
    static constraints = {
        pageId(nullable: false)
    }
}
