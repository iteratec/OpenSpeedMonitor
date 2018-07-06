import { TestBed, inject } from '@angular/core/testing';

import { CsiService } from './csi.service';

describe('CsiService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CsiService]
    });
  });

  it('should be created', inject([CsiService], (service: CsiService) => {
    expect(service).toBeTruthy();
  }));
});
