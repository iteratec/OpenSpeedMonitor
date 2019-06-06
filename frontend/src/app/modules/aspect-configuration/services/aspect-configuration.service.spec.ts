import {inject, TestBed} from '@angular/core/testing';

import {AspectConfigurationService} from './aspect-configuration.service';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {Application} from "../../../models/application.model";
import {HttpRequest} from "@angular/common/http";
import {BrowserInfoDto} from "../../../models/browser.model";
import {ApplicationService} from "../../../services/application.service";
import {ExtendedPerformanceAspect, PerformanceAspect} from "../../../models/perfomance-aspect.model";
import {BehaviorSubject, ReplaySubject} from "rxjs";
import {Page} from "../../../models/page.model";

describe('AspectConfigurationService', () => {

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AspectConfigurationService,
        {
          provide: ApplicationService,
          useValue: {
            selectedApplication$: new ReplaySubject<Application>(1),
            selectedPage$: new ReplaySubject<Page>(1),
            performanceAspectsForPage$: new BehaviorSubject<PerformanceAspect[]>([]),
            setSelectedApplication: (applicationId: string) => {
            }
          }
        }
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

  it('should bei able to get an app by id', inject(
    [HttpTestingController, AspectConfigurationService, ApplicationService],
    (httpMock: HttpTestingController, service: AspectConfigurationService, applicationService: ApplicationService) => {
      const appId = '1';
      spyOn(applicationService, 'setSelectedApplication');
      service.loadApplication(appId);
      expect(applicationService.setSelectedApplication).toHaveBeenCalledWith(appId);
    }));

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

      httpMock.expectOne("/aspectConfiguration/rest/getAspectTypes");
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

  describe('aspect extension', () => {
    it('should not provide any extended aspects if just BrowserInfos and no PerformanceAspects exist', inject(
      [AspectConfigurationService],
      (service: AspectConfigurationService) => {
        const browserInfos: BrowserInfoDto[] = [{
          browserId: 1,
          browserName: 'Chrome',
          operatingSystem: 'Windows',
          deviceType: {name: 'Desktop', icon: 'desktop'}
        }];
        const extendedAspects: ExtendedPerformanceAspect[] = service.extendAspects([], browserInfos);
        expect(extendedAspects.length).toBe(0)
      }));
    it('should not provide any extended aspects if just PerformanceAspects and no BrowserInfos exist', inject(
      [AspectConfigurationService],
      (service: AspectConfigurationService) => {
        const aspects: PerformanceAspect[] = [{
          id: 1,
          pageId: 1,
          applicationId: 1,
          browserId: 1,
          measurand: {id: 'DOC_COMPLETE', name: 'Document complete'},
          performanceAspectType: {name: 'PAGE_CONSTRUCTION_STARTED', icon: 'hourglass'},
          persistent: true,
        }];
        const extendedAspects: ExtendedPerformanceAspect[] = service.extendAspects(aspects, []);
        expect(extendedAspects.length).toBe(0)
      }));
    it('should extend aspects correctly only if matching BrowserInfo exists', inject(
      [AspectConfigurationService],
      (service: AspectConfigurationService) => {
        const idOfExtended: number = 1;
        const idOfNotExtended: number = 2;
        const aspects: PerformanceAspect[] = [
          {
            id: 1,
            pageId: 1,
            applicationId: 1,
            browserId: idOfExtended,
            measurand: {id: 'DOC_COMPLETE', name: 'Document complete'},
            performanceAspectType: {name: 'PAGE_CONSTRUCTION_STARTED', icon: 'hourglass'},
            persistent: true,
          },
          {
            id: 1,
            pageId: 1,
            applicationId: 1,
            browserId: idOfNotExtended,
            measurand: {id: 'DOC_COMPLETE', name: 'Document complete'},
            performanceAspectType: {name: 'PAGE_CONSTRUCTION_STARTED', icon: 'hourglass'},
            persistent: true,
          }
        ];
        const browserInfos: BrowserInfoDto[] = [{
          browserId: idOfExtended,
          browserName: 'Chrome',
          operatingSystem: 'Windows',
          deviceType: {name: 'Desktop', icon: 'desktop'}
        }];
        const extendedAspects: ExtendedPerformanceAspect[] = service.extendAspects(aspects, browserInfos);
        expect(extendedAspects.length).toBe(2);
        const extendedAspect: ExtendedPerformanceAspect = extendedAspects.filter((aspect: PerformanceAspect) => aspect.browserId == idOfExtended)[0];
        expect(extendedAspect.browserName).toBe('Chrome');
        expect(extendedAspect.operatingSystem).toBe('Windows');
        expect(extendedAspect.deviceType).toEqual({name: 'Desktop', icon: 'desktop'});
        const notExtendedAspect: ExtendedPerformanceAspect = extendedAspects.filter((aspect: PerformanceAspect) => aspect.browserId == idOfNotExtended)[0];
        expect(notExtendedAspect.browserName).toBe('Unknown');
        expect(notExtendedAspect.operatingSystem).toBe('Unknown');
        expect(notExtendedAspect.deviceType).toEqual({name: 'Unknown', icon: 'question'});
      }));
  });

})
;
