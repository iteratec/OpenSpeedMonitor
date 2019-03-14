import { TestBed } from '@angular/core/testing';

import { ResultSelectionService } from './result-selection.service';

describe('ResultSelectionService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ResultSelectionService = TestBed.get(ResultSelectionService);
    expect(service).toBeTruthy();
  });
});
