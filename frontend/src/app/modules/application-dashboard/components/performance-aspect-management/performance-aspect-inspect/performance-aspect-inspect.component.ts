import {Component, Input, OnInit} from '@angular/core';
import {PerformanceAspect} from "../../../../../models/perfomance-aspect.model";
import {TranslateService} from "@ngx-translate/core";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {log} from "util";

@Component({
  selector: 'osm-performance-aspect-inspect',
  templateUrl: './performance-aspect-inspect.component.html',
  styleUrls: ['./performance-aspect-inspect.component.scss']
})
export class PerformanceAspectInspectComponent implements OnInit {
  @Input() performanceAspect: PerformanceAspect;
  selectedMetric$: Observable<string>;
  editMode: boolean = false;

  constructor(private translateService: TranslateService) { }

  ngOnInit() {
    const key: string = `frontend.de.iteratec.isr.measurand.${this.performanceAspect.metricIdentifier}`;
    this.selectedMetric$ = this.translateService.get(key).pipe(
      map((translation: string) => translation === key ? this.performanceAspect.metricIdentifier : key)
    )
  }

  edit(){
    this.editMode = true;
  }

  cancel(){
    this.editMode = false;
  }

  save(){
    this.editMode = false;
  }
}
