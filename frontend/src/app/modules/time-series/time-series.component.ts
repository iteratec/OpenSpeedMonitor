import {Component, OnInit} from '@angular/core';
import {URL} from "../../enums/url.enum";
import {LinechartDataService} from "./services/linechart-data.service";
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {EventResultData, EventResultDataDTO} from './models/event-result-data.model';
import {BehaviorSubject} from 'rxjs';
import {SpinnerService} from "../shared/services/spinner.service";

@Component({
  selector: 'osm-time-series',
  templateUrl: './time-series.component.html',
  styleUrls: ['./time-series.component.scss']
})
export class TimeSeriesComponent implements OnInit {

  public results$ = new BehaviorSubject<EventResultDataDTO>(new EventResultData());
  showChart: boolean = false;

  constructor(private linechartDataService: LinechartDataService, private resultSelectionStore: ResultSelectionStore, private spinnerService: SpinnerService) { }

  ngOnInit() {
    this.showChart = false;
    if (this.resultSelectionStore.validQuery) {
      this.getTimeSeriesChartData();
    }
  }

  getTimeSeriesChartData() {
    this.showChart = true;
    this.spinnerService.showSpinner('time-series-chart-spinner');
    this.linechartDataService.fetchEventResultData<EventResultDataDTO>(
      this.resultSelectionStore.resultSelectionCommand,
      this.resultSelectionStore.remainingResultSelection,
      URL.EVENT_RESULT_DASHBOARD_LINECHART_DATA
    ).subscribe(next => {
      this.spinnerService.hideSpinner('time-series-chart-spinner');
      this.results$.next(next)
    });
  }
}
