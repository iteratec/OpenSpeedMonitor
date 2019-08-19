import { Component, OnInit } from '@angular/core';
import {URL} from "../../enums/url.enum";
import {LinechartDataService} from "./services/linechart-data.service";
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {TimeSeriesResultsDTO, TimeSeriesResults} from './models/time-series-results.model';
import {BehaviorSubject} from 'rxjs';

@Component({
  selector: 'osm-time-series',
  templateUrl: './time-series.component.html',
  styleUrls: ['./time-series.component.scss']
})
export class TimeSeriesComponent implements OnInit {

  public results$ = new BehaviorSubject<TimeSeriesResultsDTO>(new TimeSeriesResults());

  constructor(private linechartDataService: LinechartDataService, private resultSelectionStore: ResultSelectionStore) { }

  ngOnInit() {
  }

  getTimeSeriesChartData() {
    this.linechartDataService.fetchLinechartData<TimeSeriesResultsDTO>(
      this.resultSelectionStore.resultSelectionCommand,
      this.resultSelectionStore.remainingResultSelection,
      URL.EVENT_RESULT_DASHBOARD_LINECHART_DATA
    ).subscribe(next => this.results$.next(next));
  }

}
