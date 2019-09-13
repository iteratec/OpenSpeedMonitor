import { TestBed } from '@angular/core/testing';

import { LinechartDataService } from './linechart-data.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('LinechartDataService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule]
  }));

  it('should be created', () => {
    const service: LinechartDataService = TestBed.get(LinechartDataService);
    expect(service).toBeTruthy();
  });
});
