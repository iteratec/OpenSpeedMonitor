import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {MetricFinderComponent} from './metric-finder.component';
import {LineChartComponent} from './components/line-chart/line-chart.component';
import {MetricFinderService} from './services/metric-finder.service';
import {EMPTY, of} from 'rxjs';
import {FilmstripComponent} from './components/filmstrip-component/filmstrip.component';
import {FilmstripService} from './services/filmstrip.service';

describe('MetricFinderComponent', () => {
  let component: MetricFinderComponent;
  let fixture: ComponentFixture<MetricFinderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [MetricFinderComponent, FilmstripComponent, LineChartComponent],
      providers: [{
        provide: MetricFinderService,
        useClass: class {
          public testResults$ = EMPTY;
          public loadTestData = jasmine.createSpy('loadTestData');
        }
      }, {
        provide: FilmstripService,
        useClass: class {
          public filmStripData$ = of([[]]);
          public getFilmstripData = jasmine.createSpy('getFilmstripData');
          public createFilmstrip = jasmine.createSpy('createFilmstrip');
        }
      }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricFinderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    const metricService: MetricFinderService = TestBed.get(MetricFinderService);
    expect(component).toBeTruthy();
    expect(metricService.loadTestData).toHaveBeenCalled();
  });
});
