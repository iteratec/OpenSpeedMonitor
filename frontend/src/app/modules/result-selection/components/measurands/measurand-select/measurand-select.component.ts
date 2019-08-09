import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {MeasurandGroup, SelectableMeasurand, Measurand} from "../../../../../models/measurand.model";
import {ResponseWithLoadingState} from "../../../../../models/response-with-loading-state.model";
import {PerformanceAspectType} from "../../../../../models/perfomance-aspect.model";

@Component({
  selector: 'osm-measurand-select',
  templateUrl: './measurand-select.component.html',
  styleUrls: ['./measurand-select.component.scss']
})
export class MeasurandSelectComponent implements OnInit {
  @Input() selectedMeasurand: Measurand;
  @Output() onSelect: EventEmitter<Measurand> = new EventEmitter<Measurand>();

  @Input() selectableMeasurandGroups: BehaviorSubject<ResponseWithLoadingState<BehaviorSubject<MeasurandGroup>[]>>;
  @Input() perfAspectTypes$: BehaviorSubject<PerformanceAspectType[]>;


  ngOnInit() {
  }

  selectMeasurand() {
    this.onSelect.emit(this.selectedMeasurand);
  }

  compareMeasurands(measurand1: Measurand, measurand2: Measurand): boolean {
    if (measurand1 && measurand2) {
      if (measurand1.kind === "performance-aspect-type" && measurand2.kind === "performance-aspect-type") {
        return measurand1.name == measurand2.name;
      } if (measurand1.kind === "selectable-measurand" && measurand2.kind === "selectable-measurand") {
        return measurand1.id == measurand2.id;
      }
    }
    return false;
  }
}
