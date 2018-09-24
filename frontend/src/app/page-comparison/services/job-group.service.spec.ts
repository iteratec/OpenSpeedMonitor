import {inject, TestBed} from '@angular/core/testing';

import {JobGroupService} from './job-group.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('JobGroupService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        JobGroupService
      ],
      imports: [
        HttpClientTestingModule
      ]
    });
  });

  it('should be created', inject([JobGroupService], (service: JobGroupService) => {
    expect(service).toBeTruthy();
  }));
});
