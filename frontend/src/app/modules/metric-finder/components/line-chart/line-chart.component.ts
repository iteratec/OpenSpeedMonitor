import {Component, Input} from '@angular/core';
import {TestResult} from '../../models/test-result';

@Component({
  selector: 'osm-line-chart',
  templateUrl: './line-chart.component.html',
  styleUrls: ['./line-chart.component.scss']
})
export class LineChartComponent {

  @Input()
  results: TestResult[];

  constructor() { }

}
