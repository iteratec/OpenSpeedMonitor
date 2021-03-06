import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AggregationChartComponent} from './aggregation-chart.component';
import {BarchartDataService} from '../../services/barchart-data.service';
import {AggregationChartDataService} from '../../services/aggregation-chart-data.service';
import {SharedMocksModule} from '../../../../testing/shared-mocks.module';
import {ResultSelectionStore} from '../../../result-selection/services/result-selection.store';
import {ResultSelectionService} from '../../../result-selection/services/result-selection.service';
import {SharedModule} from '../../../shared/shared.module';

describe('AggregationChartComponent', () => {
  let component: AggregationChartComponent;
  let fixture: ComponentFixture<AggregationChartComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AggregationChartComponent],
      providers: [
        BarchartDataService,
        AggregationChartDataService,
        ResultSelectionStore,
        ResultSelectionService
      ],
      imports: [SharedMocksModule, SharedModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AggregationChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
