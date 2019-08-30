import {TestBed} from '@angular/core/testing';
import { AggregationChartDataService } from './aggregation-chart-data.service';
import {BarchartDataService} from "./barchart-data.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterTestingModule} from "@angular/router/testing";
import {ResultSelectionStore} from "../../result-selection/services/result-selection.store";

describe('AggregationChartDataService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [
        BarchartDataService,
        ResultSelectionStore,
        AggregationChartDataService
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule
      ]
    }));

  fit('should be created', () => {
    const service: AggregationChartDataService = TestBed.get(AggregationChartDataService);
    expect(service).toBeTruthy();
  });
});
