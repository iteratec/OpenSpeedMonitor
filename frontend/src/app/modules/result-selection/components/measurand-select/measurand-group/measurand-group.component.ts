import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ReplaySubject} from "rxjs";
import {MeasurandGroup, SelectableMeasurand} from "../../../../../models/measurand.model";
import {ResponseWithLoadingState} from "../../../../../models/response-with-loading-state.model";

@Component({
  selector: 'osm-measurand-group',
  templateUrl: './measurand-group.component.html',
  styleUrls: ['./measurand-group.component.scss']
})
export class MeasurandGroupComponent implements OnInit {
  @Input() measurandGroup$: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>>;
  @Output() onSelectMeasurand: EventEmitter<SelectableMeasurand> = new EventEmitter<SelectableMeasurand>();

  constructor() { }

  ngOnInit() {
  }

  selectMeasurand(measurand: SelectableMeasurand){
    this.onSelectMeasurand.emit(measurand);
  }
}
