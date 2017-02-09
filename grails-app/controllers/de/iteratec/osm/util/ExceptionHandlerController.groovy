package de.iteratec.osm.util

import de.iteratec.osm.annotations.RestAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus

class ExceptionHandlerController {
    static Logger log = LoggerFactory.getLogger(ControllerUtils.class)

    def handleException(Exception exception) {
        // get the controller method which was called
        def controllerAction = controllerClass.getClazz().declaredMethods.find { it.name == actionName }
        // check if the annotation "RestAction" is present to decide which response should be send
        def actionIsRestAnnotated = controllerAction.isAnnotationPresent(RestAction)


        if (actionIsRestAnnotated) {
            log.error("Error while REST call", exception)
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occured.")
        } else {
            throw exception
        }
    }
}
