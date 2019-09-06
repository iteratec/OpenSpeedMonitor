import { TestBed } from '@angular/core/testing';

import { ResultSelectionStore } from './result-selection.store';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ResultSelectionService} from "./result-selection.service";
import {RouterTestingModule} from "@angular/router/testing";

describe('ResultSelectionStore', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      ResultSelectionStore,
      ResultSelectionService
    ],
    imports: [
      HttpClientTestingModule,
      RouterTestingModule
    ]
  }));

  it('should be created', () => {
    const service: ResultSelectionStore = TestBed.get(ResultSelectionStore);
    expect(service).toBeTruthy();
  });
});
