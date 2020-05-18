import {TestBed} from '@angular/core/testing';

import {LineChartDataService} from './line-chart-data.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('LineChartDataService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule]
  }));

  it('should be created', () => {
    const service: LineChartDataService = TestBed.get(LineChartDataService);
    expect(service).toBeTruthy();
  });
});
