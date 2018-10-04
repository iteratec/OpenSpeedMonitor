import {inject, TestBed} from '@angular/core/testing';

import {ApplicationService} from './application.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('ApplicationService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ApplicationService
      ],
      imports: [
        HttpClientTestingModule
      ]
    });
  });

  it('should be created', inject([ApplicationService], (service: ApplicationService) => {
    expect(service).toBeTruthy();
  }));
});
