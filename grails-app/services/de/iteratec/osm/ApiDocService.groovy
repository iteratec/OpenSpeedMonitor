package de.iteratec.osm

/**
 * Taken from  Grails Swagger Plugin (https://github.com/ajay-kmr/swagger)
 * which contains outdated dependencies and seems to be unmaintained
 */

import com.fasterxml.jackson.core.JsonProcessingException
import grails.web.mapping.LinkGenerator
import io.swagger.annotations.Api
import io.swagger.models.Swagger
import io.swagger.servlet.Reader
import io.swagger.util.Json
import org.apache.commons.lang.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class ApiDocService implements ApplicationContextAware {
    static Logger log = LoggerFactory.getLogger(ApiDocService.class)
    Swagger swagger

    ApplicationContext applicationContext

    String generateSwaggerDocument() {
        return getJsonDocument(scanSwaggerResources())
    }

    Swagger scanSwaggerResources() {
        // Below code is written to support multi-module project.
        LinkGenerator linkGenerator = applicationContext.getBean(LinkGenerator.class)
        String host = linkGenerator.getServerBaseURL()
        host = host.replace($/http:///$, StringUtils.EMPTY)
        host = host.replace($/https:///$, StringUtils.EMPTY)
        swagger.setHost(host)
        Map<String, Object> swaggerResourcesAsMap = applicationContext.getBeansWithAnnotation(Api.class)
        List<Class> swaggerResources = swaggerResourcesAsMap.collect { it.value?.class }
        if (swaggerResources) {
            Reader.read(swagger, new HashSet<Class<?>>(swaggerResources))
        }
        return swagger
    }

    static String getJsonDocument(Swagger swagger) {
        String swaggerJson = null
        if (swagger != null) {
            try {
                swaggerJson = Json.mapper().writeValueAsString(swagger)
            } catch (JsonProcessingException e) {
                log.error("Failed to write swagger document", e);
            }
        }
        return swaggerJson
    }
}

