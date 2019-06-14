import {TestBed} from '@angular/core/testing';

import {PerformanceAspectService} from './performance-aspect.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('PerformanceAspectService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientTestingModule
    ]
  }));

  it('should be created', () => {
    const service: PerformanceAspectService = TestBed.get(PerformanceAspectService);
    expect(service).toBeTruthy();
  });
});
