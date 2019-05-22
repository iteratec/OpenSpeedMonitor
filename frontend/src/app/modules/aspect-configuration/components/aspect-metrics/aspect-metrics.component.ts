import {Component, Input, OnInit} from '@angular/core';
import {ExtendedPerformanceAspect, PerformanceAspectType} from "../../../../models/perfomance-aspect.model";
import {Observable, of} from "rxjs";

@Component({
  selector: 'osm-aspect-metrics',
  templateUrl: './aspect-metrics.component.html',
  styleUrls: ['./aspect-metrics.component.scss']
})
export class AspectMetricsComponent implements OnInit {
  @Input() aspects: ExtendedPerformanceAspect[];
  @Input() aspectType: PerformanceAspectType;
  aspectsToShow$: Observable<ExtendedPerformanceAspect[]>;

  constructor() {
  }

  ngOnInit() {
    this.aspectsToShow$ = of(this.aspects.filter((aspect: ExtendedPerformanceAspect) => aspect.performanceAspectType.name == this.aspectType.name));
  }

}
