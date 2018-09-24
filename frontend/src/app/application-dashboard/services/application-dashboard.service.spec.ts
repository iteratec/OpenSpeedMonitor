import {inject, TestBed} from '@angular/core/testing';

import {ApplicationDashboardService} from './application-dashboard.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('ApplicationDashboardService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ApplicationDashboardService
      ],
      imports: [
        HttpClientTestingModule
      ]
    });
  });

  it('should be created', inject([ApplicationDashboardService], (service: ApplicationDashboardService) => {
    expect(service).toBeTruthy();
  }));
});
