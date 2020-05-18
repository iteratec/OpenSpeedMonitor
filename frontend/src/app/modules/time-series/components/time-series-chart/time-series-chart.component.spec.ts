import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {TimeSeriesChartComponent} from './time-series-chart.component';
import {SharedMocksModule} from '../../../../testing/shared-mocks.module';
import {SharedModule} from '../../../shared/shared.module';

describe('TimeSeriesChartComponent', () => {
  let component: TimeSeriesChartComponent;
  let fixture: ComponentFixture<TimeSeriesChartComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TimeSeriesChartComponent],
      imports: [SharedMocksModule, SharedModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TimeSeriesChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
