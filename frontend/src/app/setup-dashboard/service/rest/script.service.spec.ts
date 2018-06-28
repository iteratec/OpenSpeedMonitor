import { TestBed, inject } from '@angular/core/testing';

import { ScriptService } from './script.service';

describe('ScriptService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ScriptService]
    });
  });

  it('should be created', inject([ScriptService], (service: ScriptService) => {
    expect(service).toBeTruthy();
  }));
});
