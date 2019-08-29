import { Component, OnInit } from '@angular/core';
import {EventResultData, EventResultDataDTO} from "../time-series/models/event-result-data.model";
import {URL} from "../../enums/url.enum";
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {BehaviorSubject} from "rxjs";
import {ViolinchartDataService} from "./services/violinchart-data.service";

@Component({
  selector: 'osm-distribution',
  templateUrl: './distribution.component.html',
  styleUrls: ['./distribution.component.scss']
})
export class DistributionComponent implements OnInit {

  public results$ = new BehaviorSubject<EventResultDataDTO>(new EventResultData());

  constructor(private violinChartService: ViolinchartDataService, private resultSelectionStore: ResultSelectionStore) { }

  ngOnInit() {
  }

  getDistributionChartData() {
    this.violinChartService.fetchEventResultData<EventResultDataDTO>(
      this.resultSelectionStore.resultSelectionCommand,
      this.resultSelectionStore.remainingResultSelection,
      URL.DISTRIBUTION_LINECHART_DATA
    ).subscribe(next => this.results$.next(next));
    console.log("click");
  }
}
