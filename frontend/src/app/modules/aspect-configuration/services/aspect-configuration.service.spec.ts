import {inject, TestBed} from '@angular/core/testing';

import {AspectConfigurationService} from './aspect-configuration.service';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {Application} from "../../../models/application.model";
import {HttpRequest} from "@angular/common/http";
import {BrowserInfoDto} from "../../../models/browser.model";

fdescribe('AspectConfigurationService', () => {

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AspectConfigurationService
      ],
      imports: [
        HttpClientTestingModule
      ]
    });
  });

  it('should be created', () => {
    const service: AspectConfigurationService = TestBed.get(AspectConfigurationService);
    expect(service).toBeTruthy();
  });

  it('should be able to load application by id', inject(
    [HttpTestingController, AspectConfigurationService],
    (httpMock: HttpTestingController, service: AspectConfigurationService) => {

      const appId = 1;
      const appName = "test-app";

      service.loadApplication("1").subscribe((app: Application) => {
        expect(app.id).toBe(appId);
        expect(app.name).toBe(appName);
        expect(app.pageCount).toBeNull();
        expect(app.dateOfLastResults).toBeNull();
        expect(app.csiConfigurationId).toBeNull();
      });
      const req = httpMock.expectOne((req: HttpRequest<any>) => {
        expect(req.url).toBe("/applicationDashboard/rest/getApplication");
        return true;
      });
      expect(req.request.method).toEqual('GET');
      req.flush({id: appId, name: appName});
      httpMock.verify();
    }
  ));

  it('should be able to load application by id and extracting all information from dto', inject(
    [HttpTestingController, AspectConfigurationService],
    (httpMock: HttpTestingController, service: AspectConfigurationService) => {

      const appId = 1;
      const appName = "test-app";
      const expectedDate = new Date();
      const pageCount = 5;
      const csiConfId = 53;

      service.loadApplication("1").subscribe((app: Application) => {
        expect(app.id).toBe(appId);
        expect(app.name).toBe(appName);
        expect(app.pageCount).toBe(pageCount);
        expect(app.dateOfLastResults).toEqual(expectedDate);
        expect(app.csiConfigurationId).toBe(csiConfId)
      });
      const req = httpMock.expectOne((req: HttpRequest<any>) => {
        expect(req.url).toBe("/applicationDashboard/rest/getApplication");
        return true;
      });
      expect(req.request.method).toEqual('GET');
      req.flush({
        id: appId,
        name: appName,
        pageCount: pageCount,
        dateOfLastResults: expectedDate,
        csiConfigurationId: csiConfId
      });
      httpMock.verify();
    }
  ));

  it('should provide loaded BrowserInfoDtos in an observable', inject(
    [HttpTestingController, AspectConfigurationService],
    (httpMock: HttpTestingController, service: AspectConfigurationService) => {

      const browserInfos: BrowserInfoDto[] = [
        {
          browserId: 1,
          browserName: "browser1",
          operatingSystem: 'Android',
          deviceType: {name: 'Galaxy S8', icon: 'mobile'}
        },
        {
          browserId: 2,
          browserName: "browser2",
          operatingSystem: 'Android',
          deviceType: {name: 'Galaxy Tab A', icon: 'tablet'}
        }
      ];

      service.loadBrowserInfos();
      const req = httpMock.expectOne((req: HttpRequest<any>) => {
        expect(req.url).toBe("/aspectConfiguration/rest/getBrowserInformations");
        return true;
      });
      expect(req.request.method).toEqual('GET');
      req.flush(browserInfos);
      httpMock.verify();
      const infosFromObservable: BrowserInfoDto[] = service.browserInfos$.getValue();
      expect(infosFromObservable.length).toBe(2);
      const info1: BrowserInfoDto = infosFromObservable.filter((info: BrowserInfoDto) => info.browserId == 1)[0];
      expect(info1.browserName).toBe("browser1");
      expect(info1.operatingSystem).toBe("Android");
      expect(info1.deviceType).toEqual({name: 'Galaxy S8', icon: 'mobile'});
      const info2: BrowserInfoDto = infosFromObservable.filter((info: BrowserInfoDto) => info.browserId == 2)[0];
      expect(info2.browserName).toBe("browser2");
      expect(info2.operatingSystem).toBe("Android");
      expect(info2.deviceType).toEqual({name: 'Galaxy Tab A', icon: 'tablet'});
    }))

})
;
