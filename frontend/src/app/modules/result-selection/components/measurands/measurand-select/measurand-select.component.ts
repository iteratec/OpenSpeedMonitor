import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {MeasurandGroup, SelectableMeasurand} from "../../../../../models/measurand.model";
import {ResponseWithLoadingState} from "../../../../../models/response-with-loading-state.model";
import {PerformanceAspectType} from "../../../../../models/perfomance-aspect.model";

@Component({
  selector: 'osm-measurand-select',
  templateUrl: './measurand-select.component.html',
  styleUrls: ['./measurand-select.component.scss']
})
export class MeasurandSelectComponent implements OnInit {
  @Input() selectedMeasurand: PerformanceAspectType | SelectableMeasurand;
  @Output() onSelect: EventEmitter<PerformanceAspectType | SelectableMeasurand> = new EventEmitter<PerformanceAspectType | SelectableMeasurand>();

  @Input() selectableMeasurandGroups: BehaviorSubject<ResponseWithLoadingState<BehaviorSubject<MeasurandGroup>[]>>;
  @Input() perfAspectTypes$: BehaviorSubject<PerformanceAspectType[]>;


  ngOnInit() {
  }

  selectMeasurand() {
    this.onSelect.emit(this.selectedMeasurand);
  }

  compareMeasurands(measurand1: PerformanceAspectType | SelectableMeasurand, measurand2: PerformanceAspectType | SelectableMeasurand): boolean {
    if (measurand1 && measurand2) {
      if ('id' in measurand1 && 'id' in measurand2) {
        return measurand1.id == measurand2.id;
      } else if (!('id' in measurand1 || 'id' in measurand2)) {
        return measurand1.name == measurand2.name;
      }
    }
    return measurand1 == measurand2;
  }
}
