import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {BarchartDataService} from "./services/barchart-data.service";
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {AggregationChartDataService} from "./services/aggregation-chart-data.service";
import {BehaviorSubject} from "rxjs";

@Component({
  selector: 'osm-aggregation',
  templateUrl: './aggregation.component.html',
  styleUrls: ['./aggregation.component.scss']
})
export class AggregationComponent implements OnInit {

  isHidden: boolean;
  barchartAverageData$: BehaviorSubject<any> = new BehaviorSubject<any>([]);
  barchartMedianData$: BehaviorSubject<any> = new BehaviorSubject<any>([]);

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
    this.aggregationChartDataService.isLoading$.next(false);
  }

  getBarchartData(): void {
    this.isHidden = false;
    this.aggregationChartDataService.getBarchartData(this.resultSelectionStore.resultSelectionCommand,this.resultSelectionStore.remainingResultSelection);

  }
}
