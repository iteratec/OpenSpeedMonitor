import {
  AfterContentInit,
  Component,
  ElementRef,
  HostListener,
  Input,
  OnChanges,
  SimpleChanges,
  ViewChild
} from "@angular/core";

import {EventResultData} from '../../models/event-result-data.model';
import {LineChartService} from '../../services/line-chart.service';


@Component({
  selector: 'osm-time-series-line-chart',
  templateUrl: './time-series-line-chart.component.html',
  styleUrls: [ './time-series-line-chart.component.scss' ]
})
export class TimeSeriesLineChartComponent implements AfterContentInit, OnChanges {

  @Input()
  timeSeriesResults: EventResultData;

  @ViewChild("svg")
  svgElement: ElementRef;


  constructor(
    private lineChartService: LineChartService
  ) {}

  @HostListener('window:resize', ['$event'])
  redraw() {
    this.lineChartService.drawLineChart(this.timeSeriesResults);
  }

  ngAfterContentInit(): void {
    this.lineChartService.initChart(this.svgElement);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.redraw();
  }

}
