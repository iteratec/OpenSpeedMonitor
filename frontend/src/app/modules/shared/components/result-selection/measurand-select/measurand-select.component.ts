import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ResultSelectionService} from "../../../../../services/result-selection.service";
import {ReplaySubject} from "rxjs";
import {ResponseWithLoadingState} from "../../../../../models/response-with-loading-state.model";
import {SelectableMeasurand} from "../../../../../models/measurand.model";

@Component({
  selector: 'osm-measurand-select',
  templateUrl: './measurand-select.component.html',
  styleUrls: ['./measurand-select.component.scss']
})
export class MeasurandSelectComponent implements OnInit {
  @Input() initialValue: SelectableMeasurand;
  @Output() onSelect: EventEmitter<SelectableMeasurand> = new EventEmitter<SelectableMeasurand>();

  measurands$: ReplaySubject<ResponseWithLoadingState<SelectableMeasurand[]>>;
  userTimings$: ReplaySubject<ResponseWithLoadingState<SelectableMeasurand[]>>;
  heroTimings$: ReplaySubject<ResponseWithLoadingState<SelectableMeasurand[]>>;

  constructor(private resultSelectionService: ResultSelectionService) {
    this.measurands$ = this.resultSelectionService.measurands$;
    this.userTimings$ = this.resultSelectionService.userTimings$;
    this.heroTimings$ = this.resultSelectionService.heroTimings$;
  }

  ngOnInit() {
  }

  selectMeasurand(measurand: SelectableMeasurand){
    this.initialValue = measurand;
    this.onSelect.emit(measurand);
  }

}
