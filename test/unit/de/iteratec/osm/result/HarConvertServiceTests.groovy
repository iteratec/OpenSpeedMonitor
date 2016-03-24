package de.iteratec.osm.result

import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.result.EventResultXmlService
import de.iteratec.osm.result.detail.Asset
import de.iteratec.osm.result.detail.AssetGroup
import de.iteratec.osm.result.detail.HarConvertService
import grails.test.mixin.*
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.GPathResult
import org.junit.Before
import org.junit.Test
import spock.lang.Specification

/**
 */
@TestFor(HarConvertService)
@Mock([JobGroup, JobResult, Browser,WebPageTestServer, Location, Script, Job, EventResult, Page])
class   HarConvertServiceTests extends Specification{

    private def getJson(){
        File file = new File("test/resources/HARs/HarTestArchive.har")
        new JsonSlurper().parse(file)
    }

    @Test
    public void extractAssetListForPageCountTest(){
        given:
            def json = getJson()
        when:
            List<Asset> assetsList1= service.extractAssetListForPage(json,"page_1_0")
            List<Asset> assetsList2= service.extractAssetListForPage(json,"page_1_1")

        then:
            assetsList1.size() == 16
            assetsList2.size() == 7



    }
    @Test
    public void extractAssetListForPageValueTest(){
        given:
            def json = getJson()
        when:
            Asset asset= service.extractAssetListForPage(json,"page_1_0").find({it.indexWithinHar == 0 })

        then:
            //values manually extracted from har
            asset.bytesIn == 538
            asset.bytesOut == 375
            asset.timeToFirstByteMs == 55
            asset.connectTimeMs == 32
            asset.contentType == "text/html"
            asset.downloadTimeMs == 0
            asset.fullURL == "http://google.de/"
            asset.host == "google.de"
            asset.indexWithinHar == 0
            asset.loadTimeMs == 55
            asset.mediaType == "text"
            asset.sslNegotiationTimeMs == -1
            asset.subtype == "html"

    }

    @Test
    public void createAssetGroupForPageTest(){
        given:
            new Page(name: "undefined").save()
            def json = getJson()
            def page = json.log.pages.find({it.id == "page_1_0"})
            def simpleJob = TestDataUtil.createSimpleJob()
            def jobResult = TestDataUtil.createJobResult("123",new Date(),simpleJob,simpleJob.location)
        when:
            List<Asset> assetList= service.extractAssetListForPage(json,"page_1_0")
            AssetGroup assetGroup = service.createAssetGroupForPage(page,assetList,jobResult)

        then:
            assetGroup.assets == assetList
            assetGroup.browser == simpleJob.location.browser.id
            assetGroup.jobResult == jobResult.id

    }





}
