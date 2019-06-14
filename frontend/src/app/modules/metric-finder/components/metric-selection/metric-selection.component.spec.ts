import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {MetricSelectionComponent} from './metric-selection.component';
import {MetricFinderService} from '../../services/metric-finder.service';
import {MetricFinderServiceMock} from '../../services/metric-finder.service.mock';
import {TestInfoDTO, TestResult} from '../../models/test-result.model';

describe('MetricSelectionComponent', () => {
  let component: MetricSelectionComponent;
  let fixture: ComponentFixture<MetricSelectionComponent>;
  const dummyTestInfo: TestInfoDTO = {
    testId: 'XX_YY_ZZ',
    run: 1,
    cached: false,
    step: 1,
    wptUrl: 'http://wpt'
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MetricSelectionComponent ],
      providers: [{
        provide: MetricFinderService,
        useClass: MetricFinderServiceMock
      }]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have a correct intersect method', () => {
    const lists = [
      ['a', 'b', 'c', 'd', 'e',           'x'     ],
      [     'b', 'c', 'd', 'e', 'f', 'g'          ],
      ['a',      'c', 'd', 'e', 'f', 'g', 'x'     ],
      [          'c', 'd'                         ],
      [          'c', 'd',                     'y']
    ];
    const expected = ['c', 'd'];
    // @ts-ignore
    expect(component.intersect(lists)).toEqual(expected);
  });

  it('should determine available aspectMetrics from results', () => {
    const metricService: MetricFinderService = TestBed.get(MetricFinderService);
    const now = Date.now();
    component.results = [
      new TestResult({
        date: new Date(now - 5000),
        testInfo: dummyTestInfo,
        timings: {DOC_COMPLETE: 300, _HERO_IMAGE: 400, FIRST_INTERACTIVE: 500, START_RENDER: 300, _UT_A: 900, _HERO_FIRST: 400}
      }),
      new TestResult({
        date: new Date(now),
        testInfo: dummyTestInfo,
        timings: {DOC_COMPLETE: 200, _HERO_IMAGE: 300, LAST_INTERACTIVE: 1400, START_RENDER: 300, _UT_A: 900}
      })
    ];
    expect(component.availableMetrics).toEqual([
      {
        id: 'DOC_COMPLETE',
        name: metricService.getMetricName('DOC_COMPLETE'),
        isUserTiming: false
      },
      {
        id: 'START_RENDER',
        name: metricService.getMetricName('START_RENDER'),
        isUserTiming: false
      },
      {
        id: '_HERO_IMAGE',
        name: metricService.getMetricName('_HERO_IMAGE'),
        isUserTiming: true
      },
      {
        id: '_UT_A',
        name: metricService.getMetricName('_UT_A'),
        isUserTiming: true
      }
    ]);
  });

});
