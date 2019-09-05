import { TestBed } from '@angular/core/testing';

import { ViolinchartDataService } from './violinchart-data.service';

describe('ViolinchartDataService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ViolinchartDataService = TestBed.get(ViolinchartDataService);
    expect(service).toBeTruthy();
  });
});
