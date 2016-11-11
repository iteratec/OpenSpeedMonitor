package de.iteratec.osm.api

import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.MvQueryParams
import de.iteratec.osm.result.dao.MeasuredEventDaoService
import grails.databinding.SimpleMapDataBindingSource
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.*

import javax.persistence.NoResultException

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([Page, JobGroup, Browser, Location])
class ResultsBetweenCmdMeasuredEventBindingSpec extends Specification {

    def dataBinder
    ResultsRequestCommand cmd
    SimpleMapDataBindingSource requestMap

    def setup() {

        // Use Grails data binding
        dataBinder = applicationContext.getBean('grailsWebDataBinder')

        createTestData()

        mockServicesViaMetaClassMagic()

        cmd = new ResultsRequestCommand()

        prepareRequestParameters()

    }

    void "requested single page in page parameter"() {
        given: "test specific request parameters"
        requestMap.map["page"] = "homepage"

        when: "get bound to command"
        dataBinder.bind(cmd, requestMap)
        MvQueryParams queryParams = cmd.createMvQueryParams(null, null)

        then: "command provides MvQueryParams with single page as query parameter"
        notThrown NoResultException
        queryParams.pageIds.size() == 1
        queryParams.pageIds[0] == Page.findByName("homepage").ident()
    }
    void "requested multiple pages in page parameter"() {
        given: "test specific request parameters"
        requestMap.map["page"] = "homepage,category"

        when: "get bound to command"
        dataBinder.bind(cmd, requestMap)
        MvQueryParams queryParams = cmd.createMvQueryParams(null, null)

        then: "command provides MvQueryParams with multiple pages as query parameter"
        notThrown NoResultException
        queryParams.pageIds.size() == 2
        queryParams.pageIds.containsAll([
                Page.findByName("homepage").ident(),
                Page.findByName("category").ident()
        ])
    }
    void "requested single page in pageId parameter"() {
        given: "test specific request parameters"
        requestMap.map["pageId"] = Page.findByName("homepage").ident().toString()

        when: "get bound to command"
        dataBinder.bind(cmd, requestMap)
        MvQueryParams queryParams = cmd.createMvQueryParams(null, null)

        then: "command provides MvQueryParams with single page as query parameter"
        notThrown NoResultException
        queryParams.pageIds.size() == 1
        queryParams.pageIds[0] == Page.findByName("homepage").ident()
    }
    void "requested multiple pages in pageId parameter"() {
        given: "test specific request parameters"
        requestMap.map["pageId"] = "${Page.findByName("homepage").ident()},${Page.findByName("category").ident()}"

        when: "get bound to command"
        dataBinder.bind(cmd, requestMap)
        MvQueryParams queryParams = cmd.createMvQueryParams(null, null)

        then: "command provides MvQueryParams with multiple pages as query parameter"
        notThrown NoResultException
        queryParams.pageIds.size() == 2
        queryParams.pageIds.containsAll([
                Page.findByName("homepage").ident(),
                Page.findByName("category").ident()
        ])
    }
    void "requested single page in page AND pageId parameter"() {
        given: "test specific request parameters"
        requestMap.map["page"] = "homepage"
        requestMap.map["pageId"] = Page.findByName("category").ident().toString()

        when: "get bound to command"
        dataBinder.bind(cmd, requestMap)
        MvQueryParams queryParams = cmd.createMvQueryParams(null, null)

        then: "command provides MvQueryParams with page respective pageId query parameter"
        notThrown NoResultException
        queryParams.pageIds.size() == 1
        queryParams.pageIds[0] == Page.findByName("category").ident()
    }
    void "requested single page with invalid pageId parameter"() {
        given: "test specific request parameters"
        requestMap.map["pageId"] = "homepage"

        when: "get bound to command"
        dataBinder.bind(cmd, requestMap)
        MvQueryParams queryParams = cmd.createMvQueryParams(null, null)

        then: "an exception occurs"
        NoResultException ex = thrown()
        ex.message == 'Parameter pageId must be an Integer.'
    }

    private createTestData() {
        TestDataUtil.createPage("homepage")
        TestDataUtil.createPage("category")
        TestDataUtil.createJobGroup("my-job-group")
    }

    void mockServicesViaMetaClassMagic(){
        MeasuredEventDaoService.metaClass.tryToFindByName = { String name->
            return ""
        }
    }

    private prepareRequestParameters() {
        requestMap = [
                timestampFrom: "01.01.2016",
                timestampTo  : "02.01.2016",
                system       : "my-job-group"
        ]
    }
}