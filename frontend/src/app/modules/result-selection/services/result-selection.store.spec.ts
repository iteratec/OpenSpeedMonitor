import { TestBed } from '@angular/core/testing';

import { ResultSelectionStore } from './result-selection.store';
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('ResultSelectionStore', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      ResultSelectionStore
    ],
    imports: [
      HttpClientTestingModule
    ]
  }));

  it('should be created', () => {
    const service: ResultSelectionStore = TestBed.get(ResultSelectionStore);
    expect(service).toBeTruthy();
  });
});
