import { TestBed } from '@angular/core/testing';

import { AggregationService } from './aggregation.service';

describe('AggregationService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: AggregationService = TestBed.get(AggregationService);
    expect(service).toBeTruthy();
  });
});
