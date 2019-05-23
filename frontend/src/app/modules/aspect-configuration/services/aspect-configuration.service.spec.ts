import {TestBed} from '@angular/core/testing';

import {AspectConfigurationService} from './aspect-configuration.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('AspectConfigurationService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      AspectConfigurationService
    ],
    imports: [
      HttpClientTestingModule
    ]
  }));

  it('should be created', () => {
    const service: AspectConfigurationService = TestBed.get(AspectConfigurationService);
    expect(service).toBeTruthy();
  });
});
