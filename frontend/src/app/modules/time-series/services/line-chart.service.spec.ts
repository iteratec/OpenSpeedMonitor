import {TestBed} from '@angular/core/testing';

import {LineChartService} from './line-chart.service';
import {SharedModule} from '../../shared/shared.module';
import {SharedMocksModule} from '../../../testing/shared-mocks.module';

describe('LineChartService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [SharedMocksModule, SharedModule]
  }));

  it('should be created', () => {
    const service: LineChartService = TestBed.get(LineChartService);
    expect(service).toBeTruthy();
  });
});
