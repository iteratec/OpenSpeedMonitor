import {TestBed} from '@angular/core/testing';
import {AggregationChartDataService} from './aggregation-chart-data.service';
import {BarchartDataService} from './barchart-data.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ResultSelectionStore} from '../../result-selection/services/result-selection.store';
import {ResultSelectionService} from 'src/app/modules/result-selection/services/result-selection.service';
import {SharedMocksModule} from '../../../testing/shared-mocks.module';

describe('AggregationChartDataService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [
        BarchartDataService,
        ResultSelectionStore,
        AggregationChartDataService,
        ResultSelectionService
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        SharedMocksModule
      ]
    }));

  it('should be created', () => {
    const service: AggregationChartDataService = TestBed.get(AggregationChartDataService);
    expect(service).toBeTruthy();
  });
});
