package openspeedmonitor

import de.iteratec.osm.FrontendWatcher
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.grails.config.yaml.YamlPropertySourceLoader
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.AbstractEnvironment
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

class Application extends GrailsAutoConfiguration implements ExternalConfig {
    static void main(String[] args) {
        GrailsApp.run(Application, args)

        if (grails.util.Environment.getCurrent() == grails.util.Environment.DEVELOPMENT) {
            FrontendWatcher.initializeFrontendWatcher()
        }
    }
}

// code customized from https://github.com/sbglasius/external-config with permission from sbglasius
// ... until https://github.com/sbglasius/external-config/issues/1 is done
trait ExternalConfig implements EnvironmentAware {
    private ResourceLoader defaultResourceLoader = new DefaultResourceLoader()
    private YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader()
    private String userHome = System.getProperty("user.home")
    private String appName = "OpenSpeedMonitor"

    /**
     * Set the {@code Environment} that this object runs in.
     */
    @Override
    void setEnvironment(Environment environment) {
        println "start reading external config-file"

        /*
         * locations to search for config files that get merged into the main config:
         *  config files can be ConfigSlurper scripts, Java properties files, or classes
         *  in the classpath in ConfigSlurper format
         */
        def osmConfLocationBasedOnEnvVar = System.properties["osm_config_location"]
        List<String> defaultLocations
        if (osmConfLocationBasedOnEnvVar) {
            log.info("sytem property for external configuration found: ${osmConfLocationBasedOnEnvVar}")
            defaultLocations = ["file:" +  osmConfLocationBasedOnEnvVar]
        } else {
            defaultLocations = [
                    "classpath:${appName}-config.yml",
                    "file:${userHome}/.grails/${appName}-config.yml"]
        }

        List locations = environment.getProperty('grails.config.locations', ArrayList, defaultLocations)
        String encoding = environment.getProperty('grails.config.encoding', String, 'UTF-8')

        for (location in locations) {
            Map properties = null
            if (location instanceof Class) {
                properties = loadClassConfig(location as Class)
            } else {
                String finalLocation = location.toString()
                // Replace ~ with value from system property 'user.home' if set
                if(environment.properties.systemProperties.'user.home' && finalLocation.startsWith('~/')) {
                    finalLocation = "file:${environment.properties.systemProperties.'user.home'}${finalLocation[1..-1]}"
                }
                Resource resource = defaultResourceLoader.getResource(finalLocation)
                if(resource.exists()) {
                    println "resource exists: ${resource.getFile().absolutePath}"

                    if(finalLocation.endsWith('.groovy')) {
                        properties = loadGroovyConfig(resource, encoding)
                    } else if(finalLocation.endsWith('.yml')) {
                        environment.activeProfiles
                        properties = loadYamlConfig(resource)

                    } else {
                        // Attempt to load the config as plain old properties file (POPF)
                        properties = loadPropertiesConfig(resource)
                    }
                } else {
                    println "Config file $finalLocation not found"
                }
            }
            if (properties) {
                ((AbstractEnvironment) environment).propertySources.addFirst(new MapPropertySource(location.toString(), properties))
            }
        }

    }

    private Map loadClassConfig(Class location) {
        println "Loading config class ${location.name}"
        new ConfigSlurper(grails.util.Environment.current.name).parse((Class) location)?.flatten()
    }

    private Map loadGroovyConfig(Resource resource, String encoding) {
        println "Loading groovy config file ${resource.URI}"
        String configText = resource.inputStream.getText(encoding)
        return configText ? new ConfigSlurper(grails.util.Environment.current.name).parse(configText)?.flatten() : null
    }

    private Map loadYamlConfig(Resource resource) {
        // TODO:        def yaml = new YamlPropertySourceLoader()
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean()
        yamlPropertiesFactoryBean.setResources(resource)
        yamlPropertiesFactoryBean.afterPropertiesSet()
        yamlPropertiesFactoryBean.getObject()
    }

    Map loadPropertiesConfig(Resource resource) {
        Properties properties = new Properties()
        properties.load(resource.inputStream)
        return properties
    }
}
