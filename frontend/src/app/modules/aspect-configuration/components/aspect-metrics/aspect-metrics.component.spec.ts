import {async, ComponentFixture, inject, TestBed} from '@angular/core/testing';

import {AspectMetricsComponent} from './aspect-metrics.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ExtendedPerformanceAspect, PerformanceAspectType} from "../../../../models/perfomance-aspect.model";

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
        AspectMetricsComponent
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AspectMetricsComponent);
    component = fixture.componentInstance;
    component.aspects = [];
    component.aspectType = {name: '', icon: ''};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should ', inject(
    [AspectMetricsComponent],
    (component: AspectMetricsComponent) => {
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
      component.aspects = aspectsOfTwoDifferentTypes;
      component.aspectType = type1;
      component.ngOnInit();
      component.aspectsToShow$.subscribe((aspects: ExtendedPerformanceAspect[]) => {
        expect(aspects.length).toBe(1);
        expect(aspects[0].performanceAspectType).toBe(type1);
      })
    }));
});
