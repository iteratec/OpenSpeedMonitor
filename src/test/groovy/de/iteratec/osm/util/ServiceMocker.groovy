/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* 	http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

package de.iteratec.osm.util

import de.iteratec.osm.ConfigService
import de.iteratec.osm.OsmConfigCacheService
import de.iteratec.osm.batch.Activity
import de.iteratec.osm.batch.BatchActivity
import de.iteratec.osm.batch.BatchActivityService
import de.iteratec.osm.batch.Status
import de.iteratec.osm.csi.*
import de.iteratec.osm.csi.transformation.TimeToCsMappingCacheService
import de.iteratec.osm.csi.transformation.TimeToCsMappingService
import de.iteratec.osm.dao.CriteriaSorting
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserService
import de.iteratec.osm.measurement.environment.Location
import de.iteratec.osm.measurement.environment.WebPageTestServer
import de.iteratec.osm.measurement.environment.wptserverproxy.ProxyService
import de.iteratec.osm.measurement.schedule.JobGroup
import de.iteratec.osm.report.chart.*
import de.iteratec.osm.report.external.MetricReportingService
import de.iteratec.osm.result.*
import de.iteratec.osm.result.dao.EventResultDaoService
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.web.mapping.LinkGenerator
import groovy.mock.interceptor.StubFor
import org.joda.time.DateTime

import java.util.regex.Pattern

/**
 * <p>
 * Mocks grails-Services.
 * These services get injected into instance-variables of other services in production by spring .
 * In unit-tests these services has to be mocked. To avoid duplication these mocks are assembled in this class.
 * </p>
 * @author nkuhn
 *
 */
@TestMixin(GrailsUnitTestMixin)
class ServiceMocker {

	private ServiceMocker(){}
	public static ServiceMocker create(){
		return new ServiceMocker()
	}

	//TODO: Write one generic method to mock arbitrary methods of arbitrary services:
//	void mockServiceMethod(serviceToMockInjectedServiceIn, Class serviceClassToMock, String nameOfMethodToMock){
//		def serviceMock = new StubFor(serviceClassToMock, true)
//		serviceMock.demand.nameOfMethodToMock(
//			1..10000,
//			getClosureToExecute(serviceClassToMock, nameOfMethodToMock)
//		)
//		serviceToMockInjectedServiceIn.metaClass.setAttribute(
//			this, serviceToMockInjectedServiceIn, withLowerFirstLetter(serviceClassToMock.getName()),  serviceMock.proxyInstance(), false, false)
//	}
	Closure getClosureToExecute(serviceClassToMock, nameOfMethodToMock){
		//TODO: should deliver the closure to be executed if the method nameOfMethodToMock of service serviceClassToMock is called in unit-tests
	}

    /**
     * Mocks methods of {@link BatchActivityService}.
	 * This Service always returns a BatchActivity when requested, but doesn't care for updates or if already one exists
     * @param serviceToMockIn
     *      Grails-Service with the service to mock as instance-variable.
     */
    void mockBatchActivityService(serviceToMockIn){
        BatchActivityService batchActivityService = new BatchActivityService()
		BatchActivityService.metaClass.getActiveBatchActivity = {Class c, long idWithinDomain, Activity activity, String name, boolean observe = true ->
			return new BatchActivity(
					activity: activity,
					domain: c.toString(),
					idWithinDomain: idWithinDomain,
					name: name,
					failures: 0,
					lastFailureMessage: "",
					progress: 0,
					progressWithinStage: "0",
					stage: "0",
					status: Status.ACTIVE,
					startDate: new Date(),
					successfulActions: 0)
		}
		BatchActivityService.metaClass.updateActivites = {
			//Do nothing
		}

		BatchActivityService.metaClass.runningBatch{Class c, long idWithinDomain ->false}
		BatchActivityService.metaClass.noteBatchActivityUpdate{BatchActivity activity ->
			//Do nothing
		}
		batchActivityService.timer?.cancel()
		batchActivityService.timer?.purge()
		serviceToMockIn.batchActivityService = batchActivityService
    }

	/**
	 * Mocks methods of {@link CsiAggregationUpdateEventDaoService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockCsiAggregationUpdateEventDaoService(serviceToMockIn){
		def csiAggregationUpdateEventDaoService = new CsiAggregationUpdateEventDaoService()
		csiAggregationUpdateEventDaoService.metaClass.createUpdateEvent = { Long csiAggregationId, CsiAggregationUpdateEvent.UpdateCause cause ->
				new CsiAggregationUpdateEvent(
					dateOfUpdate: new Date(),
					csiAggregationId: csiAggregationId,
					updateCause: cause
				).save(failOnError: true)

		}
		serviceToMockIn.csiAggregationUpdateEventDaoService = csiAggregationUpdateEventDaoService
	}
	/**
	 * Mocks methods of {@link CsiConfigCacheService}
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockOsmConfigCacheService(serviceToMockIn){
		def osmConfigCacheService = new OsmConfigCacheService()
		Integer minTimeToExpect = 250
		osmConfigCacheService.metaClass.getCachedMinDocCompleteTimeInMillisecs = {Double ageToleranceInHours ->
			return minTimeToExpect
		}
		Integer maxTimeToExpect = 180000
		osmConfigCacheService.metaClass.getCachedMaxDocCompleteTimeInMillisecs = {Double ageToleranceInHours ->
			return maxTimeToExpect
		}
		serviceToMockIn.osmConfigCacheService = osmConfigCacheService
	}
	/**
	 * Mocks {@link EventResultService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockEventResultService(serviceToMockIn){
		def eventResultService = new EventResultService()
		eventResultService.metaClass.isCsiRelevant = {EventResult toProof, Integer minDocTimeInMillisecs, Integer maxDocTimeInMillisecs ->
			return toProof.csByWptDocCompleteInPercent && toProof.docCompleteTimeInMillisecs &&
			(toProof.docCompleteTimeInMillisecs > minDocTimeInMillisecs &&
			toProof.docCompleteTimeInMillisecs < maxDocTimeInMillisecs)

		}
		serviceToMockIn.eventResultService = eventResultService
	}
	/**
	 * Mocks methods in {@link JobResultDaoService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockJobResultDaoService(serviceToMockIn){
		def jobResultDaoService = new JobResultDaoService()
		jobResultDaoService.metaClass.findJobResultByEventResult = { EventResult eventResult ->
			JobResult jobResult1 = JobResult.findByTestId(testIdOfJobRunCsiGroup1)
			JobResult jobResult2 = JobResult.findByTestId(testIdOfJobRunCsiGroup2)

			return (eventResult.jobResult.id == jobResult1.id) ?
				jobResult1:
				jobResult2

		}
		serviceToMockIn.jobResultDaoService = jobResultDaoService
	}
	/**
	 * Mocks methods in {@link BrowserService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockBrowserService(serviceToMockIn){
		def browserService = new BrowserService()
		browserService.metaClass.findByNameOrAlias = {String browserNameOrAlias ->
			return Browser.findByName(browserName)

		}
		serviceToMockIn.browserService = browserService
	}

	/**
	 * Mocks methods in {@link CsiAggregationUtilService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockCsiAggregationUtilService(serviceToMockIn, DateTime toReturnFromGetNowInUtc){
		def csiAggregationUtilService = new CsiAggregationUtilService()
		csiAggregationUtilService.metaClass.getNowInUtc = {	->
			return toReturnFromGetNowInUtc
		}
		serviceToMockIn.csiAggregationUtilService = csiAggregationUtilService
	}

	/**
	 * Mocks methods of {@link EventCsiAggregationService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromGetOrCalculateHourlyCsiAggregations
	 * 		To return from mocked method {@link EventCsiAggregationService#getOrCalculateHourylCsiAggregations}.
	 */
	void mockEventCsiAggregationService(serviceToMockIn, List<CsiAggregation> toReturnFromGetOrCalculateHourlyCsiAggregations){
		def eventCsiAggregationServiceMocked = new EventCsiAggregationService()
		eventCsiAggregationServiceMocked.metaClass.getHourlyCsiAggregations = { Date from, Date to, MvQueryParams mvQueryParams ->
			return 	toReturnFromGetOrCalculateHourlyCsiAggregations
		}
		serviceToMockIn.eventCsiAggregationService = eventCsiAggregationServiceMocked
	}
	/**
	 * Mocks methods of {@link PageCsiAggregationService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromGetOrCalculateWeeklyPageCsiAggregations
	 * 		List of {@link CsiAggregation}s, the method {@link PageCsiAggregationService#getOrCalculatePageCsiAggregations(java.util.Date, java.util.Date, CsiAggregationInterval,List<JobGroup>,List<Page>)} should return.
	 */
	void mockPageCsiAggregationService(serviceToMockIn, List<CsiAggregation> toReturnFromGetOrCalculateWeeklyPageCsiAggregations){
		def pageCsiAggregationServiceMocked = new PageCsiAggregationService()
		// new Version:
		pageCsiAggregationServiceMocked.metaClass.getOrCalculatePageCsiAggregations = {
			Date from, Date to, CsiAggregationInterval mvInterval, List<JobGroup> csiGroups, List<Page> pages ->
			return toReturnFromGetOrCalculateWeeklyPageCsiAggregations
		}
		serviceToMockIn.pageCsiAggregationService = pageCsiAggregationServiceMocked
	}
	/**
	 * Mocks {@link ShopCsiAggregationService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromGetOrCalculateWeeklyShopCsiAggregations
	 * 		List of {@link CsiAggregation}s, the method {@link ShopCsiAggregationService#getOrCalculateWeeklyShopCsiAggregations(java.util.Date, java.util.Date)} should return.
	 */
	void mockShopCsiAggregationService(serviceToMockIn, List<CsiAggregation> toReturnFromGetOrCalculateWeeklyShopCsiAggregations){
		def shopCsiAggregationServiceMocked = new ShopCsiAggregationService()
		shopCsiAggregationServiceMocked.metaClass.getOrCalculateWeeklyShopCsiAggregations =  { Date from, Date to ->
			return toReturnFromGetOrCalculateWeeklyShopCsiAggregations
		}
		shopCsiAggregationServiceMocked.metaClass.getOrCalculateShopCsiAggregations = { Date from, Date to, CsiAggregationInterval interval, List csiGroups ->
			return toReturnFromGetOrCalculateWeeklyShopCsiAggregations
		}
		serviceToMockIn.shopCsiAggregationService = shopCsiAggregationServiceMocked
	}
	/**
	 * Mocks methods of {@link CsiAggregationTagService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param idAsStringToJobGroupMap
	 * 		A map with id's as keys and respective JobGroups as values.
	 * @param idAsStringToMeasuredEventMap
	 * 		A map with id's as keys and respective MeasuredEvents as values.
	 *	@param idAsStringToPageMap
	 *			A map with id's as keys and respective Pages as values.
	 *	@param idAsStringToBrowserMap
	 *			A map with id's as keys and respective Browsers as values.
	 *	@param idAsStringToLocationMap
	 *			A map with id's as keys and respective Locations as values.
	 *
	 */
	void mockCsiAggregationTagService(
		serviceToMockIn,
		Map idAsStringToJobGroupMap,
		Map idAsStringToMeasuredEventMap,
		Map idAsStringToPageMap,
		Map idAsStringToBrowserMap,
		Map idAsStringToLocationMap){

		def csiAggregationTagServiceMocked = new CsiAggregationTagService()
		Pattern patternToReturn = ~/(${idAsStringToJobGroupMap.values()*.ident().join('|')});(${idAsStringToPageMap.values()*.ident().join('|')})/
		csiAggregationTagServiceMocked.metaClass.createHourlyEventTag = {
			JobGroup jobGroupParam,
			MeasuredEvent measuredEventParam,
			Page pageParam,
			Browser browserParam,
			Location locationParam ->

			return new CsiAggregationTagService().createHourlyEventTag(jobGroupParam,
				measuredEventParam,
				pageParam,
				browserParam,
				locationParam)
		}
		csiAggregationTagServiceMocked.metaClass.createEventResultTag = {
			JobGroup jobGroupParam,
			MeasuredEvent measuredEventParam,
			Page pageParam,
			Browser browserParam,
			Location locationParam ->

				return new CsiAggregationTagService().createHourlyEventTag(jobGroupParam,
						measuredEventParam,
						pageParam,
						browserParam,
						locationParam)
		}
		Pattern hourlyPattern = ~/(${idAsStringToJobGroupMap.values()*.ident().join('|')});(${idAsStringToPageMap.values()*.ident().join('|')});[^;];[^;];[^;]/
		csiAggregationTagServiceMocked.metaClass.getTagPatternForHourlyCsiAggregations = { MvQueryParams thePages ->
			return hourlyPattern;
		}
		csiAggregationTagServiceMocked.metaClass.findJobGroupOfHourlyEventTag = {String mvTag ->
			String idJobGroup = mvTag.split(";")[0]
			return idAsStringToJobGroupMap[idJobGroup]
		}
        csiAggregationTagServiceMocked.metaClass.findJobGroupOfEventResultTag = {String mvTag ->
            String idJobGroup = mvTag.split(";")[0]
            return idAsStringToJobGroupMap[idJobGroup]
        }
		csiAggregationTagServiceMocked.metaClass.findMeasuredEventOfHourlyEventTag = {String mvTag ->
			String measuredEventId = mvTag.split(";")[1]
			return idAsStringToMeasuredEventMap[measuredEventId]
		}
		csiAggregationTagServiceMocked.metaClass.findPageOfHourlyEventTag = {String mvTag ->
			String pageId = mvTag.split(";")[2]
			return idAsStringToPageMap[pageId]
		}
		csiAggregationTagServiceMocked.metaClass.findBrowserOfHourlyEventTag = {String mvTag ->
			String browserId = mvTag.split(";")[3]
			return idAsStringToBrowserMap[browserId]
		}
		csiAggregationTagServiceMocked.metaClass.findLocationOfHourlyEventTag = {String mvTag ->
			String locationId = mvTag.split(";")[4]
			return idAsStringToLocationMap[locationId]
		}
		csiAggregationTagServiceMocked.metaClass.findJobGroupOfWeeklyPageTag = {String mvTag ->
			String idJobGroup = mvTag.split(";")[0]
			return idAsStringToJobGroupMap[idJobGroup]
		}
		csiAggregationTagServiceMocked.metaClass.findPageByPageTag = {String mvTag ->
			String pageId = mvTag.split(";")[1]
			return idAsStringToPageMap[pageId]
		}
		csiAggregationTagServiceMocked.metaClass.findJobGroupOfWeeklyShopTag = {String mvTag ->
			return idAsStringToJobGroupMap[mvTag]
		}
		csiAggregationTagServiceMocked.metaClass.isValidHourlyEventTag = {String tagToProof ->
			return true // not the concern of the tests
		}
		csiAggregationTagServiceMocked.metaClass.getTagPatternForWeeklyPageCasWithJobGroupsAndPages = {
			List<JobGroup> theCsiGroups, List<Page> thePages ->
				return patternToReturn
		}
		csiAggregationTagServiceMocked.metaClass.getTagPatternForWeeklyShopCasWithJobGroups = {
			List<JobGroup> theCsiGroups ->
				return ~/(${theCsiGroups*.ident().join('|')})/
		}
		csiAggregationTagServiceMocked.metaClass.createPageAggregatorTag = { JobGroup group, Page page ->
			return group.ident()+";"+page.ident();
		}
		csiAggregationTagServiceMocked.metaClass.createShopAggregatorTag = { JobGroup group ->
			return group.ident();
		}

		csiAggregationTagServiceMocked.metaClass.createPageAggregatorTagByEventResult = {
			EventResult newResult ->
				JobGroup jobGroup1 = idAsStringToJobGroupMap.values().toList().first() //get first value
				Page page1 = idAsStringToPageMap.values().toList().first() //get first value
				String pageAggregatorTagToReturn = jobGroup1.ident()+';'+page1.ident();
				return pageAggregatorTagToReturn
		}

		serviceToMockIn.csiAggregationTagService = csiAggregationTagServiceMocked
	}
	/**
	 * Mocks methods in {@link CsTargetGraphDaoService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param labelOfActualCsTargetGraph
	 * 		The label of the {@link CsTargetGraph} to be returned from {@link CsTargetGraphDaoService#getActualCsTargetGraph()}.
	 */
	void mockCsTargetGraphDaoService(serviceToMockIn, String labelOfActualCsTargetGraph){
		def csTargetGraphDaoService = new CsTargetGraphDaoService()
		csTargetGraphDaoService.metaClass.getActualCsTargetGraph = { ->
			return CsTargetGraph.findByLabel(labelOfActualCsTargetGraph)
		}
		serviceToMockIn.csTargetGraphDaoService = csTargetGraphDaoService
	}
	/**
	 * Mocks methods of {@link LinkGenerator}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromLink
	 * 		To be returned from method {@link LinkGenerator#link()}.
	 */
	void mockLinkGenerator(serviceToMockIn, String toReturnFromLink){
		def grailsLinkGeneratorMocked = new LinkGenerator(){

			@Override
			String resource(Map params) {
				return null
			}

			@Override
			String link(Map params) {
				return toReturnFromLink
			}

			@Override
			String link(Map params, String encoding) {
				return null
			}

			@Override
			String getContextPath() {
				return null
			}

			@Override
			String getServerBaseURL() {
				return null
			}
		}
		serviceToMockIn.grailsLinkGenerator = grailsLinkGeneratorMocked
	}

	/**
	 * Mocks methods of {@link de.iteratec.osm.csi.transformation.TimeToCsMappingCacheService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param timeToCsMappings
	 * 		To be returned from method {@link de.iteratec.osm.csi.transformation.TimeToCsMappingCacheService#getMappings()}.
	 * @param frustrations
	 * 		To be returned from method {@link de.iteratec.osm.csi.transformation.TimeToCsMappingCacheService#getCustomerFrustrations(de.iteratec.osm.csi.Page)}
	 */
	void mockTimeToCsMappingCacheService(serviceToMockIn, timeToCsMappings, frustrations){
		TimeToCsMappingCacheService timeToCsMappingCacheService = new TimeToCsMappingCacheService()

		timeToCsMappingCacheService.metaClass.getMappingsFor = {Page page ->
			return timeToCsMappings
		}
		timeToCsMappingCacheService.metaClass.getCustomerFrustrations = {Page page ->
			return frustrations
		}

		serviceToMockIn.timeToCsMappingCacheService = timeToCsMappingCacheService
	}
	/**
	 * Mocks methods of {@link de.iteratec.osm.csi.transformation.TimeToCsMappingService}. The methods do not deliver
	 * sensible return values. Using tests should not depend on these values!
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockTTCsMappingService(serviceToMockIn){
		def timeToCsMappingService = new StubFor(TimeToCsMappingService, true)
		timeToCsMappingService.demand.getCustomerSatisfactionInPercent (0 .. 1000) { Integer docCompleteTime, Page testedPage, csiConfiguration ->
			return 1
		}
		timeToCsMappingService.demand.validFrustrationsExistFor(0 .. 1000) { Page testedPage ->
			//not the concern of this test
		}
        timeToCsMappingService.demand.validMappingsExistFor(0 .. 1000) { Page testedPage ->
            //not the concern of this test
        }
		serviceToMockIn.timeToCsMappingService = timeToCsMappingService.proxyInstance()
	}
	/**
	 * Mocks methods of {@link de.iteratec.osm.ConfigService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromGetDatabaseDriverClassName
	 * 		To be returned from method {@link de.iteratec.osm.ConfigService#getDatabaseDriverClassName()}.
	 */
	void mockConfigService(serviceToMockIn, String toReturnFromGetDatabaseDriverClassName, Integer toReturnFromGetDefaultMaxDownloadTimeInMinutes, CsiTransformation toReturnFromGetCsiTransformation){
		ConfigService configServiceMock = new ConfigService()
		configServiceMock.metaClass.getDatabaseDriverClassName = { ->
			return toReturnFromGetDatabaseDriverClassName
		}
		configServiceMock.metaClass.getDefaultMaxDownloadTimeInMinutes = { ->
			return toReturnFromGetDefaultMaxDownloadTimeInMinutes
		}
		configServiceMock.metaClass.getCsiTransformation = { ->
			return toReturnFromGetCsiTransformation
		}
		serviceToMockIn.configService = configServiceMock
	}
	/**
	 * Mocks methods in list methodsToMock in service of class classOfServiceToMock. That service get mocked in owning service serviceToMockIn.
	 * @param classOfServiceToMock
	 * 		Class of grails service which should be mocked.
	 * @param serviceToMockIn
	 * 		Grails service with the service to mock as instance-variable.
	 * @param methodsToMock
	 * 		List of methods that get mocked. For each method a return value is included.
	 * @see MethodToMock
	 */
	void mockService(Class classOfServiceToMock, serviceToMockIn, List<MethodToMock> methodsToMock){
		def serviceMock = new StubFor(classOfServiceToMock, true)
		methodsToMock.each{methodToMock ->
			String methodName = methodToMock.method.getName()
			if (methodToMock.method.getParameterTypes().size()==0){
				serviceMock.demand."$methodName"{ ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==1){
				serviceMock.demand."$methodName"{paramOne ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==2){
				serviceMock.demand."$methodName"{paramOne, paramTwo ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==3){
				serviceMock.demand."$methodName"{paramOne, paramTwo, paramThree ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==4){
				serviceMock.demand."$methodName"{paramOne, paramTwo, paramThree, paramFour ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==5){
				serviceMock.demand."$methodName"{paramOne, paramTwo, paramThree, paramFour, paramFive ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==6){
				serviceMock.demand."$methodName"{paramOne, paramTwo, paramThree, paramFour, paramFive, paramSix ->
					return methodToMock.toReturn
				}
			}else{
				throw new IllegalArgumentException('Nobody should write functions with more than six parameters. Please refactor your code!')
			}
		}
		String nameOfServiceField = classOfServiceToMock.getSimpleName()
		nameOfServiceField = nameOfServiceField[0].toLowerCase() + nameOfServiceField.substring(1)
		serviceToMockIn."$nameOfServiceField" = serviceMock.proxyInstance()
	}
	/**
	 * Mocks methods of{@link ProxyService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockProxyService(serviceToMockIn){
		ProxyService proxyServiceMock = new ProxyService()
		proxyServiceMock.metaClass.fetchLocations = {WebPageTestServer wptserver ->
			//do nothing, using tests will have to create necessary locations on their own
		}
		serviceToMockIn.proxyService = proxyServiceMock
	}
	/**
	 * Mocks methods of{@link PageService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockPageService(serviceToMockIn, Page pageToReturnFromGetPageByStepName, String innerStepNameToReturnFromExcludePagenamePart) {
		PageService pageServiceMock = new PageService()
		pageServiceMock.metaClass.getPageByStepName = {String stepname ->
			return pageToReturnFromGetPageByStepName
		}
		pageServiceMock.metaClass.excludePagenamePart = {String stepname ->
			return innerStepNameToReturnFromExcludePagenamePart
		}
		serviceToMockIn.pageService = pageServiceMock
	}

    /**
     * Mocks methods of{@link I18nService}.
     */
    void mockI18nService(serviceToMockIn) {
		def i18nService = new I18nService()
        i18nService.metaClass.msg {
            String msgKey, String defaultMessage, List objs = null ->
                return defaultMessage
        }
        serviceToMockIn.i18nService = i18nService
    }

    /**
     * Mocks methods of{@link PerformanceLoggingService}.
     */
    void mockPerformanceLoggingService(serviceToMockIn) {
		PerformanceLoggingService performanceLoggingService = new PerformanceLoggingService()
        performanceLoggingService.metaClass.logExecutionTime = {
            PerformanceLoggingService.LogLevel level, String description, PerformanceLoggingService.IndentationDepth indentation, Closure toMeasure ->
                toMeasure.call()
        }
        serviceToMockIn.performanceLoggingService = performanceLoggingService
    }

    /**
     * Mocks methods of{@link EventResultDaoService}.
     */
    void mockEventResultDaoService(serviceToMockIn, ArrayList<EventResult> eventResults) {
		EventResultDaoService eventResultDaoService = new EventResultDaoService()
        eventResultDaoService.metaClass.getLimitedMedianEventResultsBy = {
            Date fromDate,
            Date toDate,
            Set<CachedView> cachedViews,
            ErQueryParams queryParams,
            Map<String, Number> gtConstraints,
            Map<String, Number> ltConstraints,
            Map listCriteriaRestrictionMap,
            CriteriaSorting sorting ->
                return eventResults
        }
        serviceToMockIn.eventResultDaoService = eventResultDaoService
    }

	void mockCachingContainerService(serviceToMockIn, returnForGetDailyJobGroupsByStartDate, returnForGetDailyPagesByStartDate,
									 returnForGetDailyHeCsiAggregationMapByStartDate, returnForGetWeeklyJobGroupsByStartDate, returnForGetWeeklyPagesByStartDate,
									 returnForGetWeeklyHeCsiAggregationMapByStartDate, returnForCreateContainerFor) {

		def cachingContainerService = new StubFor(CachingContainerService)

		cachingContainerService.demand.getDailyJobGroupsByStartDate  {dailyMvsToCalculate, allJobGroups ->
			return returnForGetDailyJobGroupsByStartDate
		}
		cachingContainerService.demand.getDailyPagesByStartDate  {dailyMvsToCalculate, allPages ->
			return returnForGetDailyPagesByStartDate
		}
		cachingContainerService.demand.getDailyHeCsiAggregationMapByStartDate  {dailyMvsToCalculate, dailyJobGroupsByStartDate, dailyPagesByStartDate ->
			return returnForGetDailyHeCsiAggregationMapByStartDate
		}

		cachingContainerService.demand.getWeeklyJobGroupsByStartDate  {weeklyMvsToCalculate, allJobGroups ->
			return returnForGetWeeklyJobGroupsByStartDate
		}
		cachingContainerService.demand.getWeeklyPagesByStartDate  {weeklyMvsToCalculate, allPages ->
			return returnForGetWeeklyPagesByStartDate
		}
		cachingContainerService.demand.getWeeklyHeCsiAggregationMapByStartDate  {weeklyMvsToCalculate, weeklyJobGroupsByStartDate, weeklyPagesByStartDate ->
			return returnForGetWeeklyHeCsiAggregationMapByStartDate
		}

		cachingContainerService.demand.createContainerFor  {dpmvToCalcAndClose, allJobGroups, allPages, hemvsForDailyPageMv ->
			return returnForCreateContainerFor
		}

		serviceToMockIn.cachingContainerService = cachingContainerService.proxyInstance()
	}

	/**
	 * Mocks methods of{@link MetricReportingService}.
	 */
	void mockMetricReportingService(serviceToMockIn) {
		MetricReportingService metricReportingService = new MetricReportingService()
		metricReportingService.metaClass.reportEventResultToGraphite = {EventResult ->
			//Do Nothing
		}
		serviceToMockIn.metricReportingService = metricReportingService
	}

//	/**
//	 * Mocks methods in {@link CsiAggregationUtilService}.
//	 * @param serviceToMockIn
//	 * 		Grails-Service with the service to mock as instance-variable.
//	 */
//	void mockCsiAggregationUpdateService(serviceToMockIn){
//		def csiAggregationUpdateService = new StubFor(CsiAggregationUpdateService, true)
//		csiAggregationUpdateService.demand.createOrUpdateDependentMvs {
//			EventResult result->
//			//do nothing
//		}
//		serviceToMockIn.csiAggregationUpdateService = csiAggregationUpdateService.proxyInstance()
//	}
}
