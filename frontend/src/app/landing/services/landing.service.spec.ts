import { TestBed, inject } from '@angular/core/testing';

import { LandingService } from './landing.service';

describe('LandingService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LandingService]
    });
  });

  it('should be created', inject([LandingService], (service: LandingService) => {
    expect(service).toBeTruthy();
  }));
});
