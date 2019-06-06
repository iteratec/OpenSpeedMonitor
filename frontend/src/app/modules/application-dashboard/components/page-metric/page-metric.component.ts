import {Component, Input} from '@angular/core';
import {AspectMetric} from "../../../../enums/aspect-metric.enum";

@Component({
  selector: 'osm-page-metric',
  templateUrl: './page-metric.component.html',
  styleUrls: ['./page-metric.component.scss']
})

export class PageMetricComponent {
  @Input() metric: AspectMetric;
  @Input() value: string;

  public isAvailable(): boolean {
    return this.value !== '';
  }
}
