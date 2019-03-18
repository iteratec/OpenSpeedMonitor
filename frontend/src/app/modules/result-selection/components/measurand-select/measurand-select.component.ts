import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ResultSelectionService} from "../../../../services/result-selection.service";
import {ReplaySubject} from "rxjs";
import {ResponseWithLoadingState} from "../../../../models/response-with-loading-state.model";
import {MeasurandGroup, SelectableMeasurand} from "../../../../models/measurand.model";

@Component({
  selector: 'osm-measurand-select',
  templateUrl: './measurand-select.component.html',
  styleUrls: ['./measurand-select.component.scss']
})
export class MeasurandSelectComponent implements OnInit {
  @Input() initialValue: SelectableMeasurand;
  @Output() onSelect: EventEmitter<SelectableMeasurand> = new EventEmitter<SelectableMeasurand>();

  selectableMeasurandGroups: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>>[];

  constructor(private resultSelectionService: ResultSelectionService) {
    this.selectableMeasurandGroups = [
      this.resultSelectionService.loadTimes$,
      this.resultSelectionService.heroTimings$,
      this.resultSelectionService.userTimings$
    ]
  }

  ngOnInit() {
  }

  selectMeasurand(measurand: SelectableMeasurand){
    this.initialValue = measurand;
    this.onSelect.emit(measurand);
  }

}
