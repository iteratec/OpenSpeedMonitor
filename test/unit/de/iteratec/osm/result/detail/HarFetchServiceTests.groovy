package de.iteratec.osm.result.detail

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import co.freeside.betamax.tape.yaml.OrderedPropertyComparator
import co.freeside.betamax.tape.yaml.TapePropertyUtils
import de.iteratec.osm.ConfigService
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.wptserverproxy.HttpRequestService
import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.measurement.script.Script
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.result.JobResult
import de.iteratec.osm.util.PerformanceLoggingService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovyx.net.http.RESTClient
import org.apache.http.HttpHost
import org.junit.Rule
import org.junit.Test
import spock.lang.Specification
import org.yaml.snakeyaml.introspector.Property;

import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY


@TestFor(HarFetchService)
@Mock([JobGroup, Job, HARJob, Script, WebPageTestServer, Browser, Location, JobResult])
class HarFetchServiceTests extends Specification{

    @Rule public Recorder recorder = new Recorder(new ConfigSlurper().parse(new File('grails-app/conf/BetamaxConfig.groovy').toURL()).toProperties())

    def setup(){
        TapePropertyUtils.metaClass.sort = { Set<Property> properties, List<String> names ->
            new LinkedHashSet(properties.sort(false, new OrderedPropertyComparator(names)))
        }
        //mock HttpBuilder in HttpRequestService to use betamax-proxy
        Map betamaxProps = new ConfigSlurper().parse(new File('grails-app/conf/BetamaxConfig.groovy').toURL()).flatten()
        HttpRequestService httpRequestService = new HttpRequestService()
        httpRequestService.metaClass.getRestClient = {String urlAsString ->
            RESTClient restClient = new RESTClient(urlAsString)
            restClient.client.params.setParameter(DEFAULT_PROXY, new HttpHost(betamaxProps['betamax.proxyHost'], betamaxProps['betamax.proxyPort'], 'http'))
            return restClient
        }
        service.httpRequestService = httpRequestService

        service.metaClass.fetch = {}
        service.configService = new ConfigService()
        service.configService.metaClass.isDetailFetchingEnabled = {true}
        service.metaClass.shouldPersistHar = {true}
    }

    @Test
    public void testJobGroupPersist(){
        given:
            Job job = TestDataUtil.createSimpleJob()
            job.jobGroup.persistHar = true
            TestDataUtil.createJobResult("1", new Date(), job, job.location).save()

        when:
            service.addJobResultToQueue(job.id)

        then:
            HARJob.count() == 1
    }

    @Test
    public void testJobGroupNotPersist(){
        given:
            Job job = TestDataUtil.createSimpleJob()
            job.jobGroup.persistHar = false
            TestDataUtil.createJobResult("1", new Date(), job, job.location).save()

        when:
            service.addJobResultToQueue(job.id)

        then:
            HARJob.count() == 0
    }
    
    @Test
    @Betamax(tape = 'HarFetchServicesTests_FetchHarFromWPTInstanceTest')
    public void fetchHarFromWPTInstanceTest(){
        given:
            URL url = new URL("http://www.webpagetest.org/details.php?test=160329_QB_J41")

        when:
            Map map = service.fetchHarFromWPTInstance(url)
        then:
            map.log.version == "1.1"
            map.log.creator.name == "WebPagetest"
            map.log.creator.version == "2.19"
            map.log.pages[0].title == "Run 1, First View for http://amazon.de"
            map.log.pages[0].id == "page_1_0"
            map.log.pages[0].startedDateTime == "2016-03-29T09:50:35.000+00:00"
            map.log.pages[0]._URL == "http://amazon.de"
            map.log.pages[0]._loadTime==12785
            map.log.pages[0]._TTFB==648
            map.log.pages[0]._bytesOut==160853
            map.log.pages[0]._bytesOutDoc==75435
            map.log.pages[0]._bytesIn==7123193
            map.log.pages[0]._bytesInDoc==5239721
            map.log.pages[0]._connections==55
            map.log.pages[0]._requests==289
            map.log.pages[0]._requestsFull==289
            map.log.pages[0]._requestsDoc==179
            map.log.pages[0]._responses_200==271
            map.log.pages[0]._responses_404==0
            map.log.pages[0]._responses_other==2
            map.log.pages[0]._result==99999
            map.log.pages[0]._render==2290
            map.log.pages[0]._fullyLoaded==18514
            map.log.pages[0]._cached==0
            map.log.pages[0]._docTime==12785
            map.log.pages[0]._domTime==0
            map.log.pages[0]._score_cache==86
            map.log.pages[0]._score_cdn==91
            map.log.pages[0]._score_gzip==100
            map.log.pages[0]._score_cookies==-1
            map.log.pages[0]."_score_keep-alive"==100
            map.log.pages[0]._score_minify==-1
            map.log.pages[0]._score_combine==100
            map.log.pages[0]._score_compress==79
            map.log.pages[0]._score_etags==-1
            map.log.pages[0]._gzip_total==1960196
            map.log.pages[0]._gzip_savings==0
            map.log.pages[0]._minify_total==0
            map.log.pages[0]._minify_savings==0
            map.log.pages[0]._image_total==1677336
            map.log.pages[0]._image_savings==351949
            map.log.pages[0]._optimization_checked==1
            map.log.pages[0]._aft==0
            map.log.pages[0]._domElements==2019
            map.log.pages[0]._pageSpeedVersion=="1.9"
            map.log.pages[0]._title == 'Amazon.de: Günstige Preise für Elektronik &amp; Foto, Filme, Musik, Bücher, Games, Spielzeug &amp; mehr'
            map.log.pages[0]._titleTime == 1486
            map.log.pages[0]._loadEventStart == 12734
            map.log.pages[0]._loadEventEnd == 12763
            map.log.pages[0]._domContentLoadedEventStart == 3079
            map.log.pages[0]._domContentLoadedEventEnd == 3080
            map.log.pages[0]._lastVisualChange == 18690
            map.log.pages[0]._browser_name == "Google Chrome"
            map.log.pages[0]._browser_version == "49.0.2623.87"
            map.log.pages[0]._server_count == 1
            map.log.pages[0]._server_rtt == 119
            map.log.pages[0]._base_page_cdn == ""
            map.log.pages[0]._adult_site == 0
            map.log.pages[0]._fixed_viewport == 0
            map.log.pages[0]._score_progressive_jpeg == 0
            map.log.pages[0]._firstPaint == 2651
            map.log.pages[0]._docCPUms == 9313.26
            map.log.pages[0]._fullyLoadedCPUms == 12838.882
            map.log.pages[0]._docCPUpct == 73
            map.log.pages[0]._fullyLoadedCPUpct == 62
            map.log.pages[0]._isResponsive == -1
            map.log.pages[0]._browser_process_count == 4
            map.log.pages[0]._browser_main_memory_kb == 66516
            map.log.pages[0]._browser_other_private_memory_kb == 67696
            map.log.pages[0]._browser_working_set_kb == 134212
            map.log.pages[0]._domInteractive == 3079
            map.log.pages[0]._date == 1459245035
            map.log.pages[0]._SpeedIndex == 8797
            map.log.pages[0]._visualComplete == 18700
            map.log.pages[0]._run == 1
            map.log.pages[0]._effectiveBps == 398701
            map.log.pages[0]._effectiveBpsDoc == 431714


    }


}
