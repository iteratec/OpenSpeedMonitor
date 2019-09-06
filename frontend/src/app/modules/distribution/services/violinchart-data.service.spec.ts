import { TestBed } from '@angular/core/testing';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import { ViolinchartDataService } from './violinchart-data.service';

describe('ViolinchartDataService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [
        ViolinchartDataService,
      ],
      imports: [
        HttpClientTestingModule
      ]
  }));

  it('should be created', () => {
    const service: ViolinchartDataService = TestBed.get(ViolinchartDataService);
    expect(service).toBeTruthy();
  });
});
