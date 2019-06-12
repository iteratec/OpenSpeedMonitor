import {async, ComponentFixture, inject, TestBed} from '@angular/core/testing';

import {AspectMetricsComponent} from './aspect-metrics.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ExtendedPerformanceAspect, PerformanceAspectType} from "../../../../models/perfomance-aspect.model";
import {ApplicationService} from "../../../../services/application.service";
import {AspectConfigurationService} from "../../services/aspect-configuration.service";

describe('AspectMetricsComponent', () => {
  let component: AspectMetricsComponent;
  let fixture: ComponentFixture<AspectMetricsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AspectMetricsComponent
      ],
      imports: [
        SharedMocksModule,
        FormsModule,
        ReactiveFormsModule
      ],
      providers: [
        AspectMetricsComponent,
        ApplicationService,
        AspectConfigurationService
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AspectMetricsComponent);
    component = fixture.componentInstance;
    component.actualType = {name: 'aspect-type', icon: 'aspect-type-icon'};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should filter aspects respective asp', inject(
    [AspectMetricsComponent, AspectConfigurationService],
    (component: AspectMetricsComponent, aspectConfService: AspectConfigurationService) => {
      const type1: PerformanceAspectType = {name: 'PAGE_CONSTRUCTION_STARTED', icon: 'hourglass'};
      const type2: PerformanceAspectType = {name: 'PAGE_SHOWS_USEFUL_CONTENT', icon: 'hourglass'};
      const aspectsOfTwoDifferentTypes: ExtendedPerformanceAspect[] = [
        {
          id: 1,
          pageId: 1,
          applicationId: 1,
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
          applicationId: 1,
          browserId: 1,
          measurand: {id: 'DOC_COMPLETE', name: 'Document complete'},
          performanceAspectType: type2,
          persistent: true,
          browserName: 'browser1',
          operatingSystem: 'Android',
          deviceType: {name: 'Smartphone', icon: 'mobile'}
        }];
      component.actualType = type1;
      component.ngOnInit();
      aspectConfService.extendedAspects$.next(aspectsOfTwoDifferentTypes);
      component.aspectsToShow$.subscribe((aspects: ExtendedPerformanceAspect[]) => {
        expect(aspects.length).toBe(1);
        expect(aspects[0].performanceAspectType).toBe(type1);
      })
    }));
});
