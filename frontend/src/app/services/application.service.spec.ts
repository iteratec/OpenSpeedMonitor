import {inject, TestBed} from '@angular/core/testing';

import {ApplicationService} from './application.service';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {
  ApplicationCsi,
  ApplicationCsiById,
  ApplicationCsiDTO,
  ApplicationCsiDTOById
} from "../models/application-csi.model"
import {Application} from "../models/application.model";
import {parseDate} from "../utils/date.util";
import {FailingJob, FailingJobDTO} from "../modules/landing/models/failing-jobs.model";

const applicationCsiDTOById: ApplicationCsiDTOById = {
  1: {
    csiValues: [
      {
        date: "2018-10-14",
        csiDocComplete: 50
      },
      {
        date: "2018-10-15",
        csiDocComplete: 40
      },
    ],
    hasCsiConfiguration: true,
    hasJobResults: true,
    hasInvalidJobResults: false
  },
  3: {
    csiValues: [
      {
        date: "2018-10-14",
        csiDocComplete: 10
      },
      {
        date: "2018-10-15",
        csiDocComplete: 20
      },
    ],
    hasCsiConfiguration: true,
    hasJobResults: true,
    hasInvalidJobResults: false
  }
};

const applicationCsiMultipleValuesDto: ApplicationCsiDTO = {
  csiValues: [
    {
      date: "2018-10-12",
      csiDocComplete: 90
    },
    {
      date: "2018-10-15",
      csiDocComplete: 100
    },
    {
      date: "2018-10-16",
      csiDocComplete: 80
    },
  ],
  hasCsiConfiguration: true,
  hasJobResults: true,
  hasInvalidJobResults: false
};

const appName: string = "develop_Desktop";
const failingJobDto: FailingJobDTO = {
  job_id: 771,
  percentageFailLast5: 100,
  location: "otto-prod-netlab",
  application: appName,
  script: "OTTO_ADS und HP",
  browser: "Chrome"
};

describe('ApplicationService', () => {

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ApplicationService
      ],
      imports: [
        HttpClientTestingModule
      ]
    });
  });

  it('should be created', inject([ApplicationService], inject(
    [HttpTestingController, ApplicationService],
    (httpMock: HttpTestingController, service: ApplicationService) => {
      httpMock.expectOne("/applicationDashboard/rest/getFailingJobs").flush([]);
      expect(service).toBeTruthy();
    })));

  it('should be able to load CSI values for multiple groups', inject(
    [HttpTestingController, ApplicationService],
    (httpMock: HttpTestingController, service: ApplicationService) => {
      service.loadRecentCsiForApplications();
      httpMock.expectOne("/applicationDashboard/rest/getCsiValuesForApplications").flush(applicationCsiDTOById);
      httpMock.expectOne("/applicationDashboard/rest/getFailingJobs").flush([]);
      httpMock.expectOne("/applicationDashboard/rest/getApplications").flush([]);
      const applicationCsiById: ApplicationCsiById = service.applicationCsiById$.getValue();
      expect(applicationCsiById[1].recentCsi().csiDocComplete).toBe(40);
      expect(applicationCsiById[3].recentCsi().csiDocComplete).toBe(20);
      httpMock.verify();
    }));

  it('should be able to merge CSI values', inject(
    [HttpTestingController, ApplicationService],
    (httpMock: HttpTestingController, service: ApplicationService) => {
      service.applicationCsiById$.next({
        isLoading: false,
        1: new ApplicationCsi(applicationCsiDTOById[1]),
        3: new ApplicationCsi(applicationCsiDTOById[3])
      });
      service.selectedApplication$.next(new Application({id: 3, name: "Test"}));
      service.setSelectedApplication('3');
      httpMock
        .expectOne(request => request.url == "/applicationDashboard/rest/getCsiValuesForApplication")
        .flush(applicationCsiMultipleValuesDto);
      const applicationCsiById: ApplicationCsiById = service.applicationCsiById$.getValue();
      expect(applicationCsiById[3].recentCsi().csiDocComplete).toEqual(80);
      expect(applicationCsiById[3].csiValues.length).toEqual(4);
      expect(applicationCsiById[3].csiValues[2].date.getTime()).toEqual(parseDate("2018-10-15").getTime());
      expect(applicationCsiById[3].csiValues[2].csiDocComplete).toEqual(100);
    }));

  it('should be able to load failing jobs', inject(
    [HttpTestingController, ApplicationService],
    (httpMock: HttpTestingController, service: ApplicationService) => {
      httpMock.expectOne("/applicationDashboard/rest/getFailingJobs").flush([failingJobDto]);
      httpMock.expectOne("/applicationDashboard/rest/getApplications").flush([]);

      service.failingJobs$.subscribe(failingJobsByAppName => {
        expect(failingJobsByAppName[appName].length).toBe(1);
        const providedFailingJobOfApp = failingJobsByAppName[appName][0];
        expect(providedFailingJobOfApp).toEqual(new FailingJob(failingJobDto))
      });

      httpMock.verify();
    }));

  it('should be able to get application by id', inject(
    [HttpTestingController, ApplicationService],
    (httpMock: HttpTestingController, service: ApplicationService) => {

      const appId = 1;
      const appName = "test-app";

      service.selectedApplication$.subscribe((app: Application) => {
        expect(app.id).toBe(appId);
        expect(app.name).toBe(appName);
        expect(app.pageCount).toBeNull();
        expect(app.dateOfLastResults).toBeNull();
        expect(app.csiConfigurationId).toBeNull();
      });

      service.setSelectedApplication(String(appId));

      httpMock.expectOne("/applicationDashboard/rest/getApplications").flush([{id: appId, name: appName}]);
      expect('jasmine needs this expect because it does not see the async ones').toBeTruthy();
    }
  ));

  it('should be able to load application by id and extracting all information from dto', inject(
    [HttpTestingController, ApplicationService],
    (httpMock: HttpTestingController, service: ApplicationService) => {

      const appId = 1;
      const appName = "test-app";
      const expectedDate = new Date();
      const pageCount = 5;
      const csiConfId = 53;

      service.selectedApplication$.subscribe((app: Application) => {
        expect(app.id).toBe(appId);
        expect(app.name).toBe(appName);
        expect(app.pageCount).toBe(pageCount);
        expect(app.dateOfLastResults).toEqual(expectedDate);
        expect(app.csiConfigurationId).toBe(csiConfId)
      });

      service.setSelectedApplication(String(appId));

      httpMock.expectOne("/applicationDashboard/rest/getApplications").flush([{
        id: appId,
        name: appName,
        pageCount: pageCount,
        dateOfLastResults: expectedDate,
        csiConfigurationId: csiConfId
      }]);
      expect('jasmine needs this expect because it does not see the async ones').toBeTruthy();
    }
  ));
});
