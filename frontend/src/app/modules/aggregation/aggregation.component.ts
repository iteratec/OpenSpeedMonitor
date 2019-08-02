import {Component, OnInit} from '@angular/core';
import {URL} from "../../enums/url.enum";
import {BarchartDataService} from "./services/barchart-data.service";
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {GetBarchartCommand} from "./models/get-barchart-command.model";

@Component({
  selector: 'osm-aggregation',
  templateUrl: './aggregation.component.html',
  styleUrls: ['./aggregation.component.scss']
})
export class AggregationComponent implements OnInit {

  constructor(private barchartDataService: BarchartDataService, private resultSelectionStore: ResultSelectionStore) {
  }

  ngOnInit() {
  }

  getBarchartData(): void {
    this.barchartDataService.fetchBarchartData<GetBarchartCommand>(
      this.resultSelectionStore.resultSelectionCommand,
      this.resultSelectionStore.remainingResultSelection,
      "avg",
      URL.AGGREGATION_BARCHART_DATA
    ).subscribe((result: GetBarchartCommand) => console.log(result));

    this.barchartDataService.fetchBarchartData<GetBarchartCommand>(
      this.resultSelectionStore.resultSelectionCommand,
      this.resultSelectionStore.remainingResultSelection,
      50,
      URL.AGGREGATION_BARCHART_DATA
    ).subscribe((result: GetBarchartCommand) => console.log(result));
  }
}
