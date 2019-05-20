import { TestBed } from '@angular/core/testing';

import { AspectConfigurationService } from './aspect-configuration.service';

describe('AspectConfigurationService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: AspectConfigurationService = TestBed.get(AspectConfigurationService);
    expect(service).toBeTruthy();
  });
});
