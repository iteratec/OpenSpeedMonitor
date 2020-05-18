import {TestBed} from '@angular/core/testing';

import {LineChartLegendService} from './line-chart-legend.service';
import {SharedModule} from '../../../shared/shared.module';
import {SharedMocksModule} from '../../../../testing/shared-mocks.module';

describe('LineChartLegendService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [SharedMocksModule, SharedModule]
  }));

  it('should be created', () => {
    const service: LineChartLegendService = TestBed.get(LineChartLegendService);
    expect(service).toBeTruthy();
  });
});
