import { TestBed } from '@angular/core/testing';

import { LineChartDrawService } from './line-chart-draw.service';

describe('LineChartDrawService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: LineChartDrawService = TestBed.get(LineChartDrawService);
    expect(service).toBeTruthy();
  });
});
