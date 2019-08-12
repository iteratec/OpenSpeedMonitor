import {Component, OnInit} from '@angular/core';
import {URL} from "../../enums/url.enum";
import {BarchartDataService} from "./services/barchart-data.service";
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {BehaviorSubject} from "rxjs";
import {AggregationChartDataService} from "./services/aggregation-chart-data.service";

@Component({
  selector: 'osm-aggregation',
  templateUrl: './aggregation.component.html',
  styleUrls: ['./aggregation.component.scss']
})
export class AggregationComponent implements OnInit {

  barchartAverageData$: BehaviorSubject<any> = new BehaviorSubject<any>([]);
  barchartMedianData$: BehaviorSubject<any> = new BehaviorSubject<any>([]);
  isHidden: boolean;

  constructor(private barchartDataService: BarchartDataService, private resultSelectionStore: ResultSelectionStore, private aggregationChartDataService: AggregationChartDataService) {
    this.aggregationChartDataService.barchartAverageData$.subscribe((data) =>{
      this.barchartAverageData$.next(data);
    });
    this.aggregationChartDataService.barchartMedianData$.subscribe((data) => {
      this.barchartMedianData$.next(data);
    })
  }

  ngOnInit() {
    this.isHidden = true;
  }

  getBarchartData(): void {
    this.isHidden = false;
    this.aggregationChartDataService.getBarchartData(this.resultSelectionStore.resultSelectionCommand,this.resultSelectionStore.remainingResultSelection);
  }

  /*getBarchartData(): void {
    this.barchartDataService.fetchBarchartData<any>(
      this.resultSelectionStore.resultSelectionCommand,
      this.resultSelectionStore.remainingResultSelection,
      "avg",
      URL.AGGREGATION_BARCHART_DATA
    ).subscribe(result => this.barchartAverageData$.next(result));

    this.barchartDataService.fetchBarchartData<any>(
      this.resultSelectionStore.resultSelectionCommand,
      this.resultSelectionStore.remainingResultSelection,
      50,
      URL.AGGREGATION_BARCHART_DATA
    ).subscribe(result => this.barchartMedianData$.next(result));
  }*/
}
