import { TestBed, inject } from '@angular/core/testing';

import { ApplicationDashboardService } from './application-dashboard.service';

describe('ApplicationDashboardService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ApplicationDashboardService]
    });
  });

  it('should be created', inject([ApplicationDashboardService], (service: ApplicationDashboardService) => {
    expect(service).toBeTruthy();
  }));
});
