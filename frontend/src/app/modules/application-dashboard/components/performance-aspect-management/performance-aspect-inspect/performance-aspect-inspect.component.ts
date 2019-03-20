import {Component, Input, OnChanges, OnInit} from '@angular/core';
import {PerformanceAspect} from "../../../../../models/perfomance-aspect.model";
import {TranslateService} from "@ngx-translate/core";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {ResponseWithLoadingState} from "../../../../../models/response-with-loading-state.model";
import {SelectableMeasurand} from "../../../../../models/measurand.model";
import {ApplicationService} from "../../../../../services/application.service";

@Component({
  selector: 'osm-performance-aspect-inspect',
  templateUrl: './performance-aspect-inspect.component.html',
  styleUrls: ['./performance-aspect-inspect.component.scss']
})
export class PerformanceAspectInspectComponent implements OnInit, OnChanges {
  @Input() performanceAspectWrapped: ResponseWithLoadingState<PerformanceAspect>;
  selectedMetric$: Observable<string>;
  editMode: boolean = false;
  performanceAspectInEditing: PerformanceAspect;

  constructor(private translateService: TranslateService, private applicationService: ApplicationService) {
  }

  ngOnInit() {
    if(this.performanceAspectWrapped){
      this.setAspectInEditing();
      this.updateSelectedMetric();
    }
  }

  ngOnChanges(){
    if(this.performanceAspectWrapped){
      this.setAspectInEditing();
      this.updateSelectedMetric();
    }
  }

  private updateSelectedMetric() {
    const measurandName = this.performanceAspectWrapped.data.measurand.name;
    const key: string = `frontend.de.iteratec.isr.measurand.${measurandName}`;
    this.selectedMetric$ = this.translateService.get(key).pipe(
      map((translation: string) => translation === key ? measurandName : key)
    )
  }

  private setAspectInEditing() {
    this.performanceAspectInEditing = Object.assign({}, this.performanceAspectWrapped.data);
  }

  edit() {
    this.setAspectInEditing();
    this.editMode = true;
  }

  cancel() {
    this.setAspectInEditing();
    this.editMode = false;
  }

  save() {
    this.applicationService.createOrUpdatePerformanceAspect(this.performanceAspectInEditing);
    this.editMode = false;
  }

  selectMeasurandForAspect(measurand: SelectableMeasurand) {
    this.performanceAspectInEditing.measurand = measurand;
  }
}
