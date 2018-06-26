package de.iteratec.osm.csi

import grails.buildtestdata.TestDataBuilder
import grails.core.GrailsApplication
import org.grails.datastore.mapping.core.connections.ConnectionSource
import org.grails.orm.hibernate.HibernateDatastore
import org.hibernate.boot.MetadataBuilder
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.spi.MetadataImplementor
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.tool.hbm2ddl.SchemaExport
import spock.lang.Specification
/*
See https://stackoverflow.com/questions/16628929/grails-recreate-database-schema-for-integration-test
 */
class NonTransactionalIntegrationSpec extends Specification implements TestDataBuilder {
    static transactional = false

    GrailsApplication grailsApplication

    def cleanup() {
        HibernateDatastore hibernateDatastore = grailsApplication.mainContext.getBean("hibernateDatastore", HibernateDatastore)
        hibernateDatastore = hibernateDatastore.getDatastoreForConnection(ConnectionSource.DEFAULT)
        def serviceRegistry = ((SessionFactoryImplementor)hibernateDatastore.sessionFactory).getServiceRegistry()
                .getParentServiceRegistry()
        final MetadataSources metadataSources = new MetadataSources( serviceRegistry )
        final MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder()
        def metadata = (MetadataImplementor) metadataBuilder.build()

        def schemaExport = new SchemaExport(serviceRegistry, metadata)
        schemaExport.create(false, true)
    }

}
