import {TestBed} from '@angular/core/testing';

import {LineChartEventService} from './line-chart-event.service';
import {SharedModule} from '../../../shared/shared.module';
import {SharedMocksModule} from '../../../../testing/shared-mocks.module';

describe('LineChartEventService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [SharedMocksModule, SharedModule]
  }));

  it('should be created', () => {
    const service: LineChartEventService = TestBed.get(LineChartEventService);
    expect(service).toBeTruthy();
  });
});
