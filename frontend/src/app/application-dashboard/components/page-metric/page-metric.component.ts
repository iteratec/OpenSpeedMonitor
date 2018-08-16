import {Component, Input} from '@angular/core';

@Component({
  selector: 'osm-page-metric',
  templateUrl: './page-metric.component.html',
  styleUrls: ['./page-metric.component.scss']
})

export class PageMetricComponent {
  @Input() icon: string;
  @Input() value: number;
  @Input() unit: string;
  @Input() metric: string;
}
