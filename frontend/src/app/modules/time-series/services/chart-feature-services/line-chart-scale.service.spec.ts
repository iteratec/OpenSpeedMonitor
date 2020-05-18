import { TestBed } from '@angular/core/testing';

import { LineChartScaleService } from './line-chart-scale.service';

describe('LineChartScaleService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: LineChartScaleService = TestBed.get(LineChartScaleService);
    expect(service).toBeTruthy();
  });
});
