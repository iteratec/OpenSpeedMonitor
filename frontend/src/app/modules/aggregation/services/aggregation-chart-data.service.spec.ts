import {TestBed} from '@angular/core/testing';
import { AggregationChartDataService } from './aggregation-chart-data.service';
import {BarchartDataService} from "./barchart-data.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('AggregationChartDataService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [
        BarchartDataService,
        AggregationChartDataService
      ],
      imports: [
        HttpClientTestingModule
      ]
    }));

  it('should be created', () => {
    const service: AggregationChartDataService = TestBed.get(AggregationChartDataService);
    expect(service).toBeTruthy();
  });
});
