import { TestBed } from '@angular/core/testing';

import { JobResultDataService } from './job-result-data.service';

describe('JobResultDataService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: JobResultDataService = TestBed.get(JobResultDataService);
    expect(service).toBeTruthy();
  });
});
