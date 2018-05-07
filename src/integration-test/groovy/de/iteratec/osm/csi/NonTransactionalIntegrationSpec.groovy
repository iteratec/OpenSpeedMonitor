package de.iteratec.osm.csi

import grails.buildtestdata.TestDataBuilder
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.boot.spi.MetadataImplementor
import org.hibernate.tool.hbm2ddl.SchemaExport
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Connection

/*
See https://stackoverflow.com/a/32227118
 */
class NonTransactionalIntegrationSpec extends Specification implements TestDataBuilder {

    @Shared
    private static MetadataSources metadata
    Connection connection//Wie krieg ich die? Datasource.getConnection


    @Shared
    def grailsApplication

    static transactional = false

    def cleanup() {
        if (!metadata) {
            metadata = new MetadataSources(new StandardServiceRegistryBuilder().applySetting(
                    "hibernate.dialect", "org.hibernate.dialect.MySQLDialect").build())
            // [...] adding annotated classes to metadata here...
            configuration.addAnnotatedClass()

            Properties properties = new Properties()
            properties.setProperty 'hibernate.connection.driver_class', grailsApplication.config.dataSource.driverClassName
            properties.setProperty 'hibernate.connection.username', grailsApplication.config.dataSource.username
            properties.setProperty 'hibernate.connection.password', grailsApplication.config.dataSource.password ?:""
            properties.setProperty 'hibernate.connection.url', grailsApplication.config.dataSource.url
            properties.setProperty 'hibernate.dialect', 'org.hibernate.dialect.H2Dialect'
        }
        new SchemaExport(
                (MetadataImplementor) metadata.buildMetadata(),
                connection // pre-configured Connection here
        ).create(false, true)
    }

}
