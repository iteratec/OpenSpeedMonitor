package de.iteratec.osm

import org.springframework.beans.factory.annotation.Value

/**
 * Taken from  Grails Swagger Plugin (https://github.com/ajay-kmr/swagger)
 * which contains outdated dependencies and seems to be unmaintained
 */
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class ApiDocController {
    static responseFormats = ['json']
    static namespace = 'v1'
    static allowedMethods = [getDocuments: "GET"]

    ApiDocService apiDocService

    @Value("classpath*:**/webjars/swagger-ui/**/index.html")
    Resource[] swaggerUiResources

    def getDocuments() {
        if (request.getHeader('accept') && request.getHeader('accept').indexOf(MediaType.APPLICATION_JSON_VALUE) > -1) {
            try {
                String swaggerJson = apiDocService.generateSwaggerDocument()
                render contentType: MediaType.APPLICATION_JSON_UTF8_VALUE,
                        text: swaggerJson

            } catch (Exception e) {
                e.printStackTrace()
                render status: HttpStatus.INTERNAL_SERVER_ERROR,
                        text: 'Some error occurred'
            }
        } else {
            redirect uri: "/webjars/swagger-ui${getSwaggerUiFile()}?url=${request.getRequestURI()}"
        }
    }

    protected String getSwaggerUiFile() {
        try {
            (swaggerUiResources.getAt(0) as Resource).getURI().toString().split("/webjars/swagger-ui")[1]
        } catch (Exception e) {
            throw new Exception("Unable to find swagger ui.. Please make sure that you have added swagger ui dependency eg:-\n compile 'org.webjars:swagger-ui:2.2.8' \nin your build.gradle file", e)
        }
    }
}

