import {Component, EventEmitter, Input, OnChanges, OnInit, Output} from '@angular/core';
import {PerformanceAspect} from "../../../../../models/perfomance-aspect.model";
import {ReplaySubject} from "rxjs";
import {ResponseWithLoadingState} from "../../../../../models/response-with-loading-state.model";
import {MeasurandGroup, SelectableMeasurand} from "../../../../../models/measurand.model";
import {ResultSelectionService} from "../../../../result-selection/services/result-selection.service";

@Component({
  selector: 'osm-performance-aspect-inspect',
  templateUrl: './performance-aspect-inspect.component.html',
  styleUrls: ['./performance-aspect-inspect.component.scss']
})
export class PerformanceAspectInspectComponent implements OnInit, OnChanges {
  @Input() performanceAspectWrapped: ResponseWithLoadingState<PerformanceAspect>;
  @Output() onSelect: EventEmitter<PerformanceAspect> = new EventEmitter<PerformanceAspect>();
  metric$: ReplaySubject<SelectableMeasurand> = new ReplaySubject<SelectableMeasurand>();
  measurands: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>>[];

  constructor(private resultSelectionService: ResultSelectionService) {
    this.measurands = [
      this.resultSelectionService.loadTimes$,
      this.resultSelectionService.heroTimings$,
      this.resultSelectionService.userTimings$
    ];
  }

  ngOnInit() {
    if(this.performanceAspectWrapped){
      this.updateSelectedMetric();
    }
  }

  ngOnChanges(){
    if(this.performanceAspectWrapped){
      this.updateSelectedMetric();
    }
  }

  private updateSelectedMetric() {
    this.metric$.next(this.performanceAspectWrapped.data.measurand);
  }

  selectMeasurandForAspect(measurand: SelectableMeasurand) {
    this.performanceAspectWrapped.data.measurand = measurand;
    this.onSelect.emit(this.performanceAspectWrapped.data);
  }
}
