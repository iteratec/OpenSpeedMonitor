import { TestBed } from '@angular/core/testing';

import { AggregationChartDataService } from './aggregation-chart-data.service';

describe('AggregationChartDataService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: AggregationChartDataService = TestBed.get(AggregationChartDataService);
    expect(service).toBeTruthy();
  });
});
