import {async, ComponentFixture, inject, TestBed} from '@angular/core/testing';

import {AspectConfigurationComponent} from './aspect-configuration.component';
import {SharedMocksModule} from "../../testing/shared-mocks.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {of} from "rxjs";
import {
  ExtendedPerformanceAspect,
  PerformanceAspect,
  PerformanceAspectType
} from "../../models/perfomance-aspect.model";
import {AspectMetricsComponent} from "./components/aspect-metrics/aspect-metrics.component";
import {ApplicationService} from "../../services/application.service";
import {AspectConfigurationService} from "./services/aspect-configuration.service";
import {BrowserInfoDto} from "../../models/browser.model";

fdescribe('AspectConfigurationComponent', () => {
  let component: AspectConfigurationComponent;
  let fixture: ComponentFixture<AspectConfigurationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AspectConfigurationComponent,
        AspectMetricsComponent
      ],
      imports: [
        SharedMocksModule,
        FormsModule,
        ReactiveFormsModule
      ],
      providers: [
        AspectConfigurationComponent,
        ApplicationService,
        AspectConfigurationService
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AspectConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should provide unique AspectTypes correctly according to given PerformanceAspects', inject(
    [AspectConfigurationComponent],
    (component: AspectConfigurationComponent) => {
      const type1: PerformanceAspectType = {name: 'PAGE_CONSTRUCTION_STARTED', icon: 'hourglass'};
      const type2: PerformanceAspectType = {name: 'PAGE_SHOWS_USEFUL_CONTENT', icon: 'hourglass'};
      const aspectsOfTwoDifferentTypes = [
        {
          id: 1,
          pageId: 1,
          jobGroupId: 1,
          browserId: 1,
          measurand: {id: 'DOC_COMPLETE', name: 'Document complete'},
          performanceAspectType: type1,
          persistent: true,
          browserName: 'browser1',
          operatingSystem: 'Android',
          deviceType: {name: 'Smartphone', icon: 'mobile'}
        },
        {
          id: 1,
          pageId: 2,
          jobGroupId: 1,
          browserId: 1,
          measurand: {id: 'DOC_COMPLETE', name: 'Document complete'},
          performanceAspectType: type1,
          persistent: true,
          browserName: 'browser1',
          operatingSystem: 'Android',
          deviceType: {name: 'Smartphone', icon: 'mobile'}
        },
        {
          id: 1,
          pageId: 3,
          jobGroupId: 1,
          browserId: 1,
          measurand: {id: 'DOC_COMPLETE', name: 'Document complete'},
          performanceAspectType: type1,
          persistent: true,
          browserName: 'browser1',
          operatingSystem: 'Android',
          deviceType: {name: 'Smartphone', icon: 'mobile'}
        },
        {
          id: 1,
          pageId: 4,
          jobGroupId: 1,
          browserId: 1,
          measurand: {id: 'DOC_COMPLETE', name: 'Document complete'},
          performanceAspectType: type2,
          persistent: true,
          browserName: 'browser1',
          operatingSystem: 'Android',
          deviceType: {name: 'Smartphone', icon: 'mobile'}
        }];
      component.performanceAspects$ = of(aspectsOfTwoDifferentTypes);
      component.initAspectTypes();
      component.aspectTypes$.subscribe((aspectTypes: PerformanceAspectType[]) => {
        expect(aspectTypes.length).toBe(2);
        expect(aspectTypes.filter((type: PerformanceAspectType) => type == type1).length).toBe(1);
        expect(aspectTypes.filter((type: PerformanceAspectType) => type == type2).length).toBe(1);
      })
    }));

  describe('aspect extension', () => {
    it('should not provide any extended aspects if just BrowserInfos and no PerformanceAspects exist', inject(
      [AspectConfigurationComponent],
      (component: AspectConfigurationComponent) => {
        const browserInfos: BrowserInfoDto[] = [{
          browserId: 1,
          browserName: 'Chrome',
          operatingSystem: 'Windows',
          deviceType: {name: 'Desktop', icon: 'desktop'}
        }];
        const extendedAspects: ExtendedPerformanceAspect[] = component.extendAspects([], browserInfos);
        expect(extendedAspects.length).toBe(0)
      }));
    it('should not provide any extended aspects if just PerformanceAspects and no BrowserInfos exist', inject(
      [AspectConfigurationComponent],
      (component: AspectConfigurationComponent) => {
        const aspects: PerformanceAspect[] = [{
          id: 1,
          pageId: 1,
          jobGroupId: 1,
          browserId: 1,
          measurand: {id: 'DOC_COMPLETE', name: 'Document complete'},
          performanceAspectType: {name: 'PAGE_CONSTRUCTION_STARTED', icon: 'hourglass'},
          persistent: true,
        }];
        const extendedAspects: ExtendedPerformanceAspect[] = component.extendAspects(aspects, []);
        expect(extendedAspects.length).toBe(0)
      }));
    it('should extend aspects correctly only if matching BrowserInfo exists', inject(
      [AspectConfigurationComponent],
      (component: AspectConfigurationComponent) => {
        const idOfExtended: number = 1;
        const idOfNotExtended: number = 2;
        const aspects: PerformanceAspect[] = [
          {
            id: 1,
            pageId: 1,
            jobGroupId: 1,
            browserId: idOfExtended,
            measurand: {id: 'DOC_COMPLETE', name: 'Document complete'},
            performanceAspectType: {name: 'PAGE_CONSTRUCTION_STARTED', icon: 'hourglass'},
            persistent: true,
          },
          {
            id: 1,
            pageId: 1,
            jobGroupId: 1,
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
        const extendedAspects: ExtendedPerformanceAspect[] = component.extendAspects(aspects, browserInfos);
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

});
