import {Component, Input} from '@angular/core';
import {Metric} from "../../../shared/enums/metric.enum";

@Component({
  selector: 'osm-page-metric',
  templateUrl: './page-metric.component.html',
  styleUrls: ['./page-metric.component.scss']
})

export class PageMetricComponent {
  @Input() metric: Metric;
  @Input() value: string;

  public isAvailable(): boolean {
    return this.value !== '';
  }
}
