import { TestBed } from '@angular/core/testing';

import { ResultSelectionService } from './result-selection.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('ResultSelectionStore', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      ResultSelectionService
    ],
    imports: [
      HttpClientTestingModule
    ]
  }));

  it('should be created', () => {
    const service: ResultSelectionService = TestBed.get(ResultSelectionService);
    expect(service).toBeTruthy();
  });
});
