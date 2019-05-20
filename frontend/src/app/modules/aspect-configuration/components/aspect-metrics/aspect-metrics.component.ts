import {Component, Input, OnInit} from '@angular/core';
import {ResponseWithLoadingState} from "../../../../models/response-with-loading-state.model";
import {PerformanceAspect} from "../../../../models/perfomance-aspect.model";

@Component({
  selector: 'osm-aspect-metrics',
  templateUrl: './aspect-metrics.component.html',
  styleUrls: ['./aspect-metrics.component.scss']
})
export class AspectMetricsComponent implements OnInit {
  @Input() performanceAspectWrapped: ResponseWithLoadingState<PerformanceAspect>;
  constructor() { }

  ngOnInit() {
  }

}
