import { TestBed, inject } from '@angular/core/testing';

import { JobGroupRestService } from './job-group-rest.service';

describe('JobGroupRestService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [JobGroupRestService]
    });
  });

  it('should be created', inject([JobGroupRestService], (service: JobGroupRestService) => {
    expect(service).toBeTruthy();
  }));
});
