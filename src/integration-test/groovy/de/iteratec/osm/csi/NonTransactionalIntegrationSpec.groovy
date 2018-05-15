package de.iteratec.osm.csi

import org.grails.orm.hibernate.cfg.DefaultGrailsDomainConfiguration
import org.hibernate.cfg.Configuration
import org.hibernate.tool.hbm2ddl.SchemaExport
import spock.lang.Shared
import spock.lang.Specification

class NonTransactionalIntegrationSpec extends Specification {

    @Shared
    private static Configuration _configuration

    @Shared
    def grailsApplication

    static transactional = false

    def setup() {
        if (!_configuration) {
            // 1-time creation of the configuration
            Properties properties = new Properties()
            properties.setProperty 'hibernate.connection.driver_class', grailsApplication.config.dataSource.driverClassName
            properties.setProperty 'hibernate.connection.username', grailsApplication.config.dataSource.username
            properties.setProperty 'hibernate.connection.password', grailsApplication.config.dataSource.password ?:""
            properties.setProperty 'hibernate.connection.url', grailsApplication.config.dataSource.url
            properties.setProperty 'hibernate.dialect', 'org.hibernate.dialect.H2Dialect'

            _configuration = new DefaultGrailsDomainConfiguration(grailsApplication: grailsApplication, properties: properties)
        }
        new SchemaExport(_configuration).create(false, true)
    }

    def cleanup() {
      if (!_configuration) {
          // 1-time creation of the configuration
          Properties properties = new Properties()
          properties.setProperty 'hibernate.connection.driver_class', grailsApplication.config.dataSource.driverClassName
          properties.setProperty 'hibernate.connection.username', grailsApplication.config.dataSource.username
          properties.setProperty 'hibernate.connection.password', grailsApplication.config.dataSource.password ?:""
          properties.setProperty 'hibernate.connection.url', grailsApplication.config.dataSource.url

          _configuration = new DefaultGrailsDomainConfiguration(grailsApplication: grailsApplication, properties: properties)
      }
      new SchemaExport(_configuration).create(false, true)
    }

}
