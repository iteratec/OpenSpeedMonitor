import { TestBed, inject } from '@angular/core/testing';

import { JobGroupService } from './job-group.service';

describe('JobGroupService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [JobGroupService]
    });
  });

  it('should be created', inject([JobGroupService], (service: JobGroupService) => {
    expect(service).toBeTruthy();
  }));
});
