import { Component, OnInit } from '@angular/core';
import {URL} from "../../enums/url.enum";
import {LinechartDataService} from "./services/linechart-data.service";
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {GetLinechartCommand} from "./models/get-line-chart-command.model";

@Component({
  selector: 'osm-time-series',
  templateUrl: './time-series.component.html',
  styleUrls: ['./time-series.component.scss']
})
export class TimeSeriesComponent implements OnInit {

  constructor(private linechartDataService: LinechartDataService, private resultSelectionStore: ResultSelectionStore) { }

  ngOnInit() {
  }

  getTimeSeriesChartData() {
    this.linechartDataService.fetchLinechartData<GetLinechartCommand>(
      this.resultSelectionStore.resultSelectionCommand,
      this.resultSelectionStore.remainingResultSelection,
      URL.EVENT_RESULT_DASHBOARD_LINECHART_DATA
    ).subscribe((result: GetLinechartCommand) => console.log(result));
  }

}
