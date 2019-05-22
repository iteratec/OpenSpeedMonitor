import {Component, Input, OnInit} from '@angular/core';
import {ExtendedPerformanceAspect} from "../../../../models/perfomance-aspect.model";

@Component({
  selector: 'osm-aspect-metrics',
  templateUrl: './aspect-metrics.component.html',
  styleUrls: ['./aspect-metrics.component.scss']
})
export class AspectMetricsComponent implements OnInit {
  @Input() performanceAspectWrapped: ExtendedPerformanceAspect;
  constructor() { }

  ngOnInit() {
  }

}
