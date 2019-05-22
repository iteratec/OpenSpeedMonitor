import {Component, OnInit} from '@angular/core';
import {MeasurandGroup, SelectableMeasurand} from "../../../../models/measurand.model";
import {ReplaySubject} from "rxjs";
import {ResultSelectionService} from "../../services/result-selection.service";
import {ResponseWithLoadingState} from "../../../../models/response-with-loading-state.model";

@Component({
  selector: 'osm-measurands',
  templateUrl: './measurands.component.html',
  styleUrls: ['./measurands.component.scss']
})
export class MeasurandsComponent implements OnInit {
  measurands: ReplaySubject<ResponseWithLoadingState<MeasurandGroup>>[];

  selectedMeasurands: SelectableMeasurand[] = [];
  defaultValue: SelectableMeasurand;

  constructor(private resultSelectionService: ResultSelectionService) {
    this.measurands = [
      this.resultSelectionService.loadTimes$,
      this.resultSelectionService.userTimings$,
      this.resultSelectionService.heroTimings$,
      this.resultSelectionService.requestCounts$,
      this.resultSelectionService.requestSizes$,
      this.resultSelectionService.percentages$
    ];

    this.resultSelectionService.loadTimes$.subscribe(next => {
      if(next) {
        this.defaultValue = next.data.values[0];
        this.selectedMeasurands = [this.defaultValue];
      }
    });
  }

  ngOnInit() {
  }

  selectMeasurand(index: number, measurand: SelectableMeasurand) {
    this.selectedMeasurands[index] = measurand;
  }

  addMeasurandField() {
    this.selectedMeasurands.push(this.defaultValue);
  }

  removeMeasurandField(index: number) {
    this.selectedMeasurands.splice(index, 1);
  }

  trackByFn(index: number, item: any) {
    return index;
  }
}
