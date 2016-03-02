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
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.joda.time.DateTime

import java.text.DecimalFormat
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
//		def serviceMock = mockFor(serviceClassToMock, true)
//		serviceMock.demand.nameOfMethodToMock(
//			1..10000, 
//			getClosureToExecute(serviceClassToMock, nameOfMethodToMock)
//		) 
//		serviceToMockInjectedServiceIn.metaClass.setAttribute(
//			this, serviceToMockInjectedServiceIn, withLowerFirstLetter(serviceClassToMock.getName()),  serviceMock.createMock(), false, false)
//	}
	Closure getClosureToExecute(serviceClassToMock, nameOfMethodToMock){
		//TODO: should deliver the closure to be executed if the method nameOfMethodToMock of service serviceClassToMock is called in unit-tests 
	}

    /**
     * Mocks methods of {@link BatchActivityService}.
     * @param serviceToMockIn
     *      Grails-Service with the service to mock as instance-variable.
     */
    void mockBatchActivityService(serviceToMockIn){
        def batchActivityService = mockFor(BatchActivityService, true)
        HashMap<Long, Class> containingIds = new HashMap<>()

        batchActivityService.demand.getActiveBatchActivity(1..10000) {
            Class c, long idWithinDomain, Activity activity, String name, boolean observe = true ->
                containingIds.put(idWithinDomain, c)
                return new BatchActivity(
                        activity: activity,
                        domain: c.toString(),
                        idWithinDomain: idWithinDomain,
                        name: name,
                        failures: 0,
                        lastFailureMessage: "",
                        progress: 0,
                        progressWithinStage: "",
                        stage: "",
                        status: Status.ACTIVE,
                        startDate: new Date(),
                        successfulActions: 0,
                ).save(failOnError: true)
        }

        batchActivityService.demand.runningBatch(1..10000) {
            Class c,long idWithinDomain ->
                return containingIds.containsKey(idWithinDomain) ? (containingIds.get(idWithinDomain) == c ? true : false) : false
        }

        batchActivityService.demand.updateStatus(1..1000){
            BatchActivity activity,Map<String,Object> map ->
            log.info "BatchActivity status updated"
        }

        batchActivityService.demand.calculateProgress(1..1000){
            int count, int actual ->
            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(100.0/count*actual) + " %";
        }

        serviceToMockIn.batchActivityService = batchActivityService.createMock()
    }
	
	/**
	 * Mocks methods of {@link CsiAggregationUpdateEventDaoService}.
	 * @param serviceToMockIn 
	 * 		Grails-Service with the service to mock as instance-variable. 
	 */
	void mockCsiAggregationUpdateEventDaoService(serviceToMockIn){
		def csiAggregationUpdateEventDaoService = mockFor(CsiAggregationUpdateEventDaoService, true)
		csiAggregationUpdateEventDaoService.demand.createUpdateEvent(1..10000) {
			Long csiAggregationId, CsiAggregationUpdateEvent.UpdateCause cause ->
			
				new CsiAggregationUpdateEvent(
					dateOfUpdate: new Date(),
					csiAggregationId: csiAggregationId,
					updateCause: cause
				).save(failOnError: true)
				
		}
		serviceToMockIn.csiAggregationUpdateEventDaoService = csiAggregationUpdateEventDaoService.createMock()
	}
	/**
	 * Mocks methods of {@link CsiConfigCacheService}
	 * @param serviceToMockIn 
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockOsmConfigCacheService(serviceToMockIn){
		def osmConfigCacheService = mockFor(OsmConfigCacheService, true)
		Integer minTimeToExpect = 250
		osmConfigCacheService.demand.getCachedMinDocCompleteTimeInMillisecs(1..10000) {
			Double ageToleranceInHours ->
			return minTimeToExpect
		}
		Integer maxTimeToExpect = 180000
		osmConfigCacheService.demand.getCachedMaxDocCompleteTimeInMillisecs(1..10000) {
			Double ageToleranceInHours ->
			return maxTimeToExpect
		}
		serviceToMockIn.osmConfigCacheService = osmConfigCacheService.createMock()
	}
	/**
	 * Mocks {@link EventResultService}.
	 * @param serviceToMockIn 
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockEventResultService(serviceToMockIn){
		def eventResultService = mockFor(EventResultService, true)
		eventResultService.demand.isCsiRelevant(1..10000) {
			EventResult toProof, Integer minDocTimeInMillisecs, Integer maxDocTimeInMillisecs ->
			
			return toProof.csByWptDocCompleteInPercent && toProof.docCompleteTimeInMillisecs &&
			(toProof.docCompleteTimeInMillisecs > minDocTimeInMillisecs &&
			toProof.docCompleteTimeInMillisecs < maxDocTimeInMillisecs)
			
		}
		serviceToMockIn.eventResultService = eventResultService.createMock()
	}
	/**
	 * Mocks methods in {@link JobResultDaoService}.
	 * @param serviceToMockIn 
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockJobResultDaoService(serviceToMockIn){
		def jobResultDaoService = mockFor(JobResultDaoService, true)
		jobResultDaoService.demand.findJobResultByEventResult(1..10000) {
			EventResult eventResult ->
			
			JobResult jobResult1 = JobResult.findByTestId(testIdOfJobRunCsiGroup1)
			JobResult jobResult2 = JobResult.findByTestId(testIdOfJobRunCsiGroup2)
			
			return (eventResult.jobResult.id == jobResult1.id) ?
				jobResult1:
				jobResult2
			
			/*
			return jobResult1.eventResults.contains(eventResult)?
				jobResult1:
				jobResult2
			//*/
			
		}
		serviceToMockIn.jobResultDaoService = jobResultDaoService.createMock()
	}
	/**
	 * Mocks methods in {@link BrowserService}.
	 * @param serviceToMockIn 
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockBrowserService(serviceToMockIn){
		def browserService = mockFor(BrowserService, true)
		browserService.demand.findByNameOrAlias(1..10000) {
			String browserNameOrAlias ->
			return Browser.findByName(browserName)
			
		}
		serviceToMockIn.browserService = browserService.createMock()
	}
	
	/**
	 * Mocks methods in {@link CsiAggregationUtilService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockCsiAggregationUtilService(serviceToMockIn, DateTime toReturnFromGetNowInUtc){
		def csiAggregationUtilService = mockFor(CsiAggregationUtilService, true)
		csiAggregationUtilService.demand.getNowInUtc(1..10000) {
			->
			return toReturnFromGetNowInUtc
		}
		serviceToMockIn.csiAggregationUtilService = csiAggregationUtilService.createMock()
	}
	
	/**
	 * Mocks methods of {@link EventCsiAggregationService}.
	 * @param serviceToMockIn 
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromGetOrCalculateHourlyCsiAggregations
	 * 		To return from mocked method {@link EventCsiAggregationService#getOrCalculateHourylCsiAggregations}.
	 */
	void mockEventCsiAggregationService(serviceToMockIn, List<CsiAggregation> toReturnFromGetOrCalculateHourlyCsiAggregations){
		def eventCsiAggregationServiceMocked = mockFor(EventCsiAggregationService, true)
		eventCsiAggregationServiceMocked.demand.getHourlyCsiAggregations(0..10000) { Date from, Date to, MvQueryParams mvQueryParams ->
			return 	toReturnFromGetOrCalculateHourlyCsiAggregations
		}
		serviceToMockIn.eventCsiAggregationService = eventCsiAggregationServiceMocked.createMock()
	}
	/**
	 * Mocks methods of {@link PageCsiAggregationService}.
	 * @param serviceToMockIn 
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromGetOrCalculateWeeklyPageCsiAggregations
	 * 		List of {@link CsiAggregation}s, the method {@link PageCsiAggregationService#getOrCalculateWeeklyPageCsiAggregations(java.util.Date, java.util.Date)} should return.
	 */
	void mockPageCsiAggregationService(serviceToMockIn, List<CsiAggregation> toReturnFromGetOrCalculateWeeklyPageCsiAggregations){
		def pageCsiAggregationServiceMocked = mockFor(PageCsiAggregationService)
		// new Version:
		pageCsiAggregationServiceMocked.demand.getOrCalculatePageCsiAggregations(0..10000) {
			Date from, Date to, CsiAggregationInterval mvInterval, List<JobGroup> csiGroups, List<Page> pages ->
			return toReturnFromGetOrCalculateWeeklyPageCsiAggregations
		}
		serviceToMockIn.pageCsiAggregationService = pageCsiAggregationServiceMocked.createMock()
	}
	/**
	 * Mocks {@link ShopCsiAggregationService}.
	 * @param serviceToMockIn 
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromGetOrCalculateWeeklyShopCsiAggregations
	 * 		List of {@link CsiAggregation}s, the method {@link ShopCsiAggregationService#getOrCalculateWeeklyShopCsiAggregations(java.util.Date, java.util.Date)} should return.
	 */
	void mockShopCsiAggregationService(serviceToMockIn, List<CsiAggregation> toReturnFromGetOrCalculateWeeklyShopCsiAggregations){
		def shopCsiAggregationServiceMocked = mockFor(ShopCsiAggregationService, true)
		shopCsiAggregationServiceMocked.demand.getOrCalculateWeeklyShopCsiAggregations(0..10000) { Date from, Date to ->
			return toReturnFromGetOrCalculateWeeklyShopCsiAggregations
		}
		shopCsiAggregationServiceMocked.demand.getOrCalculateShopCsiAggregations(0..10000) { Date from, Date to, CsiAggregationInterval interval, List csiGroups ->
			return toReturnFromGetOrCalculateWeeklyShopCsiAggregations
		}
		serviceToMockIn.shopCsiAggregationService = shopCsiAggregationServiceMocked.createMock()
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

		def csiAggregationTagServiceMocked = mockFor(CsiAggregationTagService, true)
		Pattern patternToReturn = ~/(${idAsStringToJobGroupMap.values()*.ident().join('|')});(${idAsStringToPageMap.values()*.ident().join('|')})/
		csiAggregationTagServiceMocked.demand.createHourlyEventTag(1..10000) {
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
		csiAggregationTagServiceMocked.demand.createEventResultTag(1..10000) {
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
		csiAggregationTagServiceMocked.demand.getTagPatternForHourlyCsiAggregations(0..10000) { MvQueryParams thePages ->
			return hourlyPattern;
		}
		csiAggregationTagServiceMocked.demand.findJobGroupOfHourlyEventTag(0..10000) {String mvTag ->
			String idJobGroup = mvTag.split(";")[0]
			return idAsStringToJobGroupMap[idJobGroup]
		}
        csiAggregationTagServiceMocked.demand.findJobGroupOfEventResultTag(0..10000) {String mvTag ->
            String idJobGroup = mvTag.split(";")[0]
            return idAsStringToJobGroupMap[idJobGroup]
        }
		csiAggregationTagServiceMocked.demand.findMeasuredEventOfHourlyEventTag(0..10000) {String mvTag ->
			String measuredEventId = mvTag.split(";")[1]
			return idAsStringToMeasuredEventMap[measuredEventId]
		}
		csiAggregationTagServiceMocked.demand.findPageOfHourlyEventTag(0..10000) {String mvTag ->
			String pageId = mvTag.split(";")[2]
			return idAsStringToPageMap[pageId]
		}
		csiAggregationTagServiceMocked.demand.findBrowserOfHourlyEventTag(0..10000) {String mvTag ->
			String browserId = mvTag.split(";")[3]
			return idAsStringToBrowserMap[browserId]
		}
		csiAggregationTagServiceMocked.demand.findLocationOfHourlyEventTag(0..10000) {String mvTag ->
			String locationId = mvTag.split(";")[4]
			return idAsStringToLocationMap[locationId]
		}
		csiAggregationTagServiceMocked.demand.findJobGroupOfWeeklyPageTag(0..10000) {String mvTag ->
			String idJobGroup = mvTag.split(";")[0]
			return idAsStringToJobGroupMap[idJobGroup]
		}
		csiAggregationTagServiceMocked.demand.findPageByPageTag(0..10000) {String mvTag ->
			String pageId = mvTag.split(";")[1]
			return idAsStringToPageMap[pageId]
		}
		csiAggregationTagServiceMocked.demand.findJobGroupOfWeeklyShopTag(0..10000) {String mvTag ->
			return idAsStringToJobGroupMap[mvTag]
		}
		csiAggregationTagServiceMocked.demand.isValidHourlyEventTag(1..10000) {String tagToProof ->
			return true // not the concern of the tests
		}
		csiAggregationTagServiceMocked.demand.getTagPatternForWeeklyPageCasWithJobGroupsAndPages(0..10000) {
			List<JobGroup> theCsiGroups, List<Page> thePages ->
				return patternToReturn
		}
		csiAggregationTagServiceMocked.demand.getTagPatternForWeeklyShopCasWithJobGroups(0..10000) {
			List<JobGroup> theCsiGroups ->
				return ~/(${theCsiGroups*.ident().join('|')})/
		}
		csiAggregationTagServiceMocked.demand.createPageAggregatorTag(0..10000) { JobGroup group, Page page ->
			return group.ident()+";"+page.ident();
		}
		csiAggregationTagServiceMocked.demand.createShopAggregatorTag(0..10000) { JobGroup group ->
			return group.ident();
		}

		csiAggregationTagServiceMocked.demand.createPageAggregatorTagByEventResult(0..10000) {
			EventResult newResult ->
				JobGroup jobGroup1 = idAsStringToJobGroupMap.values().toList().first() //get first value
				Page page1 = idAsStringToPageMap.values().toList().first() //get first value
				String pageAggregatorTagToReturn = jobGroup1.ident()+';'+page1.ident();
				return pageAggregatorTagToReturn
		}

		serviceToMockIn.csiAggregationTagService = csiAggregationTagServiceMocked.createMock()
	}
	/**
	 * Mocks methods in {@link CsTargetGraphDaoService}.
	 * @param serviceToMockIn 
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param labelOfActualCsTargetGraph
	 * 		The label of the {@link CsTargetGraph} to be returned from {@link CsTargetGraphDaoService#getActualCsTargetGraph()}. 
	 */
	void mockCsTargetGraphDaoService(serviceToMockIn, String labelOfActualCsTargetGraph){
		def csTargetGraphDaoService = mockFor(CsTargetGraphDaoService, true)
		csTargetGraphDaoService.demand.getActualCsTargetGraph(0..10000) { ->
			return CsTargetGraph.findByLabel(labelOfActualCsTargetGraph)
		}
		serviceToMockIn.csTargetGraphDaoService = csTargetGraphDaoService.createMock()
	}
	/**
	 * Mocks methods of {@link LinkGenerator}.
	 * @param serviceToMockIn 
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromLink
	 * 		To be returned from method {@link LinkGenerator#link()}. 
	 */
	void mockLinkGenerator(serviceToMockIn, String toReturnFromLink){
		def grailsLinkGeneratorMocked = mockFor(LinkGenerator, true)
		grailsLinkGeneratorMocked.demand.link(0..10000) { Map params ->
			return 	toReturnFromLink
		}
		serviceToMockIn.grailsLinkGenerator = grailsLinkGeneratorMocked.createMock()
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
		def timeToCsMappingCacheService = mockFor(TimeToCsMappingCacheService)

		timeToCsMappingCacheService.demand.getMappingsFor(0..100000) {Page page ->
			return timeToCsMappings
		}
		timeToCsMappingCacheService.demand.getCustomerFrustrations(0..100000) {Page page ->
			return frustrations
		}

		serviceToMockIn.timeToCsMappingCacheService = timeToCsMappingCacheService.createMock()
	}
	/**
	 * Mocks methods of {@link de.iteratec.osm.csi.transformation.TimeToCsMappingService}. The methods do not deliver
	 * sensible return values. Using tests should not depend on these values!
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockTTCsMappingService(serviceToMockIn){
		def timeToCsMappingService = mockFor(TimeToCsMappingService, true)
		timeToCsMappingService.demand.getCustomerSatisfactionInPercent(0..100) { Integer docCompleteTime, Page testedPage, csiConfiguration ->
			return 1
		}
		timeToCsMappingService.demand.validFrustrationsExistFor(0..100) { Page testedPage ->
			//not the concern of this test
		}
        timeToCsMappingService.demand.validMappingsExistFor(0..100) { Page testedPage ->
            //not the concern of this test
        }
		serviceToMockIn.timeToCsMappingService = timeToCsMappingService.createMock()
	}
	/**
	 * Mocks methods of {@link de.iteratec.osm.ConfigService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 * @param toReturnFromGetDatabaseDriverClassName
	 * 		To be returned from method {@link de.iteratec.osm.ConfigService#getDatabaseDriverClassName()}.
	 */
	void mockConfigService(serviceToMockIn, String toReturnFromGetDatabaseDriverClassName, Integer toReturnFromGetDefaultMaxDownloadTimeInMinutes, CsiTransformation toReturnFromGetCsiTransformation){
		def configServiceMock = mockFor(ConfigService, true)
		configServiceMock.demand.getDatabaseDriverClassName(0..100){ ->
			return toReturnFromGetDatabaseDriverClassName
		}
		configServiceMock.demand.getDefaultMaxDownloadTimeInMinutes(0..100){ ->
			return toReturnFromGetDefaultMaxDownloadTimeInMinutes
		}
		configServiceMock.demand.getCsiTransformation(0..100){ ->
			return toReturnFromGetCsiTransformation
		}
		serviceToMockIn.configService = configServiceMock.createMock()
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
		def serviceMock = mockFor(classOfServiceToMock, true)
		methodsToMock.each{methodToMock ->
			String methodName = methodToMock.method.getName()
			if (methodToMock.method.getParameterTypes().size()==0){
				serviceMock.demand."$methodName"(0..100){ ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==1){
				serviceMock.demand."$methodName"(0..100){paramOne ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==2){
				serviceMock.demand."$methodName"(0..100){paramOne, paramTwo ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==3){
				serviceMock.demand."$methodName"(0..100){paramOne, paramTwo, paramThree ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==4){
				serviceMock.demand."$methodName"(0..100){paramOne, paramTwo, paramThree, paramFour ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==5){
				serviceMock.demand."$methodName"(0..100){paramOne, paramTwo, paramThree, paramFour, paramFive ->
					return methodToMock.toReturn
				}
			}else if (methodToMock.method.getParameterTypes().size()==6){
				serviceMock.demand."$methodName"(0..100){paramOne, paramTwo, paramThree, paramFour, paramFive, paramSix ->
					return methodToMock.toReturn
				}
			}else{
				throw new IllegalArgumentException('Nobody should write functions with more than six parameters. Please refactor your code!')
			}
		}
		String nameOfServiceField = classOfServiceToMock.getSimpleName()
		nameOfServiceField = nameOfServiceField[0].toLowerCase() + nameOfServiceField.substring(1)
		serviceToMockIn."$nameOfServiceField" = serviceMock.createMock()
	}
	/**
	 * Mocks methods of{@link ProxyService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockProxyService(serviceToMockIn){
		def proxyServiceMock = mockFor(ProxyService, true)
		proxyServiceMock.demand.fetchLocations(0..100){WebPageTestServer wptserver ->
			//do nothing, using tests will have to create necessary locations on their own
		}
		serviceToMockIn.proxyService = proxyServiceMock.createMock()
	}
	/**
	 * Mocks methods of{@link PageService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockPageService(serviceToMockIn, Page pageToReturnFromGetPageByStepName, String innerStepNameToReturnFromExcludePagenamePart) {
		def pageServiceMock = mockFor(PageService, true)
		pageServiceMock.demand.getPageByStepName(0..100){String stepname ->
			return pageToReturnFromGetPageByStepName
		}
		pageServiceMock.demand.excludePagenamePart(0..100){String stepname ->
			return innerStepNameToReturnFromExcludePagenamePart
		}
		serviceToMockIn.pageService = pageServiceMock.createMock()
	}

    /**
     * Mocks methods of{@link I18nService}.
     */
    void mockI18nService(serviceToMockIn) {
        def i18nService = mockFor(I18nService, true)
        i18nService.demand.msg(1..10000) {
            String msgKey, String defaultMessage, List objs ->
                return defaultMessage
        }
        serviceToMockIn.i18nService = i18nService.createMock()
    }

    /**
     * Mocks methods of{@link PerformanceLoggingService}.
     */
    void mockPerformanceLoggingService(serviceToMockIn) {
        def performanceLoggingService = mockFor(PerformanceLoggingService, true)
        performanceLoggingService.demand.logExecutionTime(1..10000) {
            PerformanceLoggingService.LogLevel level, String description, PerformanceLoggingService.IndentationDepth indentation, Closure toMeasure ->
                toMeasure.call()
        }
        serviceToMockIn.performanceLoggingService = performanceLoggingService.createMock()
    }

    /**
     * Mocks methods of{@link EventResultDaoService}.
     */
    void mockEventResultDaoService(serviceToMockIn, ArrayList<EventResult> eventResults) {
        def eventResultDaoService = mockFor(EventResultDaoService, true)
        eventResultDaoService.demand.getLimitedMedianEventResultsBy(1..10000) {
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
        serviceToMockIn.eventResultDaoService = eventResultDaoService.createMock()
    }

	void mockCachingContainerService(serviceToMockIn, returnForGetDailyJobGroupsByStartDate, returnForGetDailyPagesByStartDate,
									 returnForGetDailyHeCsiAggregationMapByStartDate, returnForGetWeeklyJobGroupsByStartDate, returnForGetWeeklyPagesByStartDate,
									 returnForGetWeeklyHeCsiAggregationMapByStartDate, returnForCreateContainerFor) {
		def cachingContainerService = mockFor(CachingContainerService, true)

		cachingContainerService.demand.getDailyJobGroupsByStartDate(0..100000){dailyMvsToCalculate, allJobGroups ->
			return returnForGetDailyJobGroupsByStartDate
		}
		cachingContainerService.demand.getDailyPagesByStartDate(0..100000){dailyMvsToCalculate, allPages ->
			return returnForGetDailyPagesByStartDate
		}
		cachingContainerService.demand.getDailyHeCsiAggregationMapByStartDate(0..100000){dailyMvsToCalculate, dailyJobGroupsByStartDate, dailyPagesByStartDate ->
			return returnForGetDailyHeCsiAggregationMapByStartDate
		}

		cachingContainerService.demand.getWeeklyJobGroupsByStartDate(0..100000){weeklyMvsToCalculate, allJobGroups ->
			return returnForGetWeeklyJobGroupsByStartDate
		}
		cachingContainerService.demand.getWeeklyPagesByStartDate(0..100000){weeklyMvsToCalculate, allPages ->
			return returnForGetWeeklyPagesByStartDate
		}
		cachingContainerService.demand.getWeeklyHeCsiAggregationMapByStartDate(0..100000){weeklyMvsToCalculate, weeklyJobGroupsByStartDate, weeklyPagesByStartDate ->
			return returnForGetWeeklyHeCsiAggregationMapByStartDate
		}

		cachingContainerService.demand.createContainerFor(0..100000){dpmvToCalcAndClose, allJobGroups, allPages, hemvsForDailyPageMv ->
			return returnForCreateContainerFor
		}

		serviceToMockIn.cachingContainerService = cachingContainerService.createMock()
	}

	/**
	 * Mocks methods of{@link MetricReportingService}.
	 */
	void mockMetricReportingService(serviceToMockIn) {
		def metricReportingService = mockFor(MetricReportingService, true)
		metricReportingService.demand.reportEventResultToGraphite(1..10000) {
			EventResult result ->
				//do nothing
		}
		serviceToMockIn.metricReportingService = metricReportingService.createMock()
	}

	/**
	 * Mocks methods in {@link CsiAggregationUtilService}.
	 * @param serviceToMockIn
	 * 		Grails-Service with the service to mock as instance-variable.
	 */
	void mockCsiAggregationUpdateService(serviceToMockIn){
		def csiAggregationUpdateService = mockFor(CsiAggregationUpdateService, true)
		csiAggregationUpdateService.demand.createOrUpdateDependentMvs(1..10000) {
			EventResult result->
			//do nothing
		}
		serviceToMockIn.csiAggregationUpdateService = csiAggregationUpdateService.createMock()
	}
}
