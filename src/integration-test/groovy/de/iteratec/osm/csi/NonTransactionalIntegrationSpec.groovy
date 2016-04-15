package de.iteratec.osm.csi

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.grails.orm.hibernate.cfg.DefaultGrailsDomainConfiguration
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import org.hibernate.tool.hbm2ddl.SchemaExport
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Rollback
class NonTransactionalIntegrationSpec extends Specification {

    @Shared
    private static Configuration _configuration

    @Shared
    def grailsApplication

    static transactional = false

    def setupSpec() {
        if (!_configuration) {
            // 1-time creation of the configuration
            Properties properties = new Properties()
            properties.setProperty 'hibernate.connection.driver_class', grailsApplication.config.dataSource.driverClassName
            properties.setProperty 'hibernate.connection.username', grailsApplication.config.dataSource.username
            properties.setProperty 'hibernate.connection.password', grailsApplication.config.dataSource.password
            properties.setProperty 'hibernate.connection.url', grailsApplication.config.dataSource.url
            properties.setProperty 'hibernate.dialect', 'org.hibernate.dialect.H2Dialect'

            _configuration = new DefaultGrailsDomainConfiguration(grailsApplication: grailsApplication, properties: properties)
        }
    }

    def cleanupSpec() {
        //After spec nuke and pave the test db
        new SchemaExport(_configuration).create(false, true)

        //Clear the sessions
        SessionFactory sf = grailsApplication.getMainContext().getBean('sessionFactory')
        sf.getCurrentSession().clear()
    }

}
