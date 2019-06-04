import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {MeasurandGroup, SelectableMeasurand} from "../../../../models/measurand.model";
import {ResponseWithLoadingState} from "../../../../models/response-with-loading-state.model";

@Component({
  selector: 'osm-measurand-select',
  templateUrl: './measurand-select.component.html',
  styleUrls: ['./measurand-select.component.scss']
})
export class MeasurandSelectComponent implements OnInit {
  @Input() selectedMeasurand: SelectableMeasurand;
  @Output() onSelect: EventEmitter<SelectableMeasurand> = new EventEmitter<SelectableMeasurand>();

  @Input() selectableMeasurandGroups: BehaviorSubject<ResponseWithLoadingState<BehaviorSubject<MeasurandGroup>[]>>;

  ngOnInit() {
  }

  selectMeasurand() {
    this.onSelect.emit(this.selectedMeasurand);
  }

  compareMeasurands(measurand1: SelectableMeasurand, measurand2: SelectableMeasurand): boolean {
    return measurand1 && measurand2 ? measurand1.id == measurand2.id : measurand1 == measurand2;
  }
}
