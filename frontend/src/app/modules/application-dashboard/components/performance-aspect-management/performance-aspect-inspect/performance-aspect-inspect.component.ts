import {Component, Input, OnInit} from '@angular/core';
import {PerformanceAspect} from "../../../../../models/perfomance-aspect.model";

@Component({
  selector: 'osm-performance-aspect-inspect',
  templateUrl: './performance-aspect-inspect.component.html',
  styleUrls: ['./performance-aspect-inspect.component.scss']
})
export class PerformanceAspectInspectComponent implements OnInit {
  @Input() performanceAspect: PerformanceAspect;

  constructor() { }

  ngOnInit() {
  }

}
