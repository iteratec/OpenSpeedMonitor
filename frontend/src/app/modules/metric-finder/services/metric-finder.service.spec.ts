import { TestBed } from '@angular/core/testing';

import { MetricFinderService } from './metric-finder.service';

describe('MetricFinderService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: MetricFinderService = TestBed.get(MetricFinderService);
    expect(service).toBeTruthy();
  });
});
