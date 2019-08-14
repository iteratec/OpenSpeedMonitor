import {Component, OnInit} from '@angular/core';
import {BarchartDataService} from "./services/barchart-data.service";
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {AggregationChartDataService} from "./services/aggregation-chart-data.service";

@Component({
  selector: 'osm-aggregation',
  templateUrl: './aggregation.component.html',
  styleUrls: ['./aggregation.component.scss']
})
export class AggregationComponent implements OnInit {

  constructor(private barchartDataService: BarchartDataService, private resultSelectionStore: ResultSelectionStore, private aggregationChartDataService: AggregationChartDataService) {
  }

  ngOnInit() {
  }

  getBarchartData(): void {
    this.aggregationChartDataService.getBarchartData(this.resultSelectionStore.resultSelectionCommand,this.resultSelectionStore.remainingResultSelection);
  }
}
