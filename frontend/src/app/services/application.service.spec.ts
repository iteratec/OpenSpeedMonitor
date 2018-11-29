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

const failingJob: Object = {
  develop_desktop: {
    job_id: 771,
    percentageFailLast5: 100,
    location: "otto-prod-netlab",
    application: "develop_Desktop",
    script: "OTTO_ADS und HP",
    browser: "Chrome"
  }
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

  it('should be created', inject([ApplicationService], (service: ApplicationService) => {
    expect(service).toBeTruthy();
  }));

  it('should be able to load CSI values for multiple groups', inject(
    [HttpTestingController, ApplicationService],
    (httpMock: HttpTestingController, service: ApplicationService) => {
      service.loadRecentCsiForApplications();
      httpMock.expectOne("/applicationDashboard/rest/getCsiValuesForApplications").flush(applicationCsiDTOById);
      httpMock.expectOne("/applicationDashboard/rest/getFailingJobs").flush([]);
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
      service.updateSelectedApplication(new Application({id: 3, name: "Test"}));
      httpMock
        .expectOne(request => request.url == "/applicationDashboard/rest/getCsiValuesForApplication")
        .flush(applicationCsiMultipleValuesDto);
      // httpMock.expectOne("/applicationDashboard/rest/getCsiValuesForPages?applicationId=3").flush(applicationCsiMultipleValuesDto);
      const applicationCsiById: ApplicationCsiById = service.applicationCsiById$.getValue();
      expect(applicationCsiById[3].recentCsi().csiDocComplete).toEqual(80);
      expect(applicationCsiById[3].csiValues.length).toEqual(4);
      expect(applicationCsiById[3].csiValues[2].date.getTime()).toEqual(parseDate("2018-10-15").getTime());
      expect(applicationCsiById[3].csiValues[2].csiDocComplete).toEqual(100);
    }));

  it('should be able to load failing jobs', inject(
    [HttpTestingController, ApplicationService],
    (httpMock: HttpTestingController, service: ApplicationService) => {
      service.failingJobs$.next(failingJob);
      service.getFailingJobs();
      httpMock.expectOne("/applicationDashboard/rest/getFailingJobs").flush(failingJob);
      service.failingJobs$.subscribe(next => expect(next).toBe(failingJob));
      httpMock.verify();
    }));
});

