package de.iteratec.osm.result

import de.iteratec.osm.annotations.RestAction
import de.iteratec.osm.csi.Page
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.result.dao.PerformanceAspectTypeDto
import de.iteratec.osm.util.ControllerUtils
import grails.validation.Validateable
import org.springframework.http.HttpStatus

class AspectConfigurationController {

    BrowserService browserService

    @RestAction
    def getPage(PageCommand cmd) {
        Page page = Page.get(cmd.pageId)
        if (page) {
            ControllerUtils.sendObjectAsJSON(response, page)
        } else {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.NOT_FOUND, "No Page with id '${cmd.pageId}' could be found.")
        }
    }

    @RestAction
    def getBrowserInformations() {
        ControllerUtils.sendObjectAsJSON(response, browserService.getBrowserInfos())
    }

    @RestAction
    def getAspectTypes() {
        List<PerformanceAspectTypeDto> types = PerformanceAspectType.values().collect {
            new PerformanceAspectTypeDto(name: it.toString(), icon: it.icon)
        }
        ControllerUtils.sendObjectAsJSON(response, types)
    }
}

class PageCommand implements Validateable {
    Long pageId
    static constraints = {
        pageId(nullable: false)
    }
}
