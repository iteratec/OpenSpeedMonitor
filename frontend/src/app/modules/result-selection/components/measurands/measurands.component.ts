import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {MeasurandGroup, SelectableMeasurand} from "../../../../models/measurand.model";
import {Observable, ReplaySubject} from "rxjs";
import {ResultSelectionService} from "../../services/result-selection.service";
import {ResponseWithLoadingState} from "../../../../models/response-with-loading-state.model";

@Component({
  selector: 'osm-measurands',
  templateUrl: './measurands.component.html',
  styleUrls: ['./measurands.component.scss']
})
export class MeasurandsComponent implements OnInit {

  loadTimes$: Observable<MeasurandGroup>;
  userTimings$: Observable<MeasurandGroup>;
  heroTimings$: Observable<MeasurandGroup>;
  requestCounts$: Observable<MeasurandGroup>;
  requestSizes$: Observable<MeasurandGroup>;
  percentages$: Observable<MeasurandGroup>;

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

    this.resultSelectionService.userTimings$.subscribe(next => {
      if(next) {
        this.measurands = [
          this.resultSelectionService.loadTimes$,
          this.resultSelectionService.userTimings$,
          this.resultSelectionService.heroTimings$,
          this.resultSelectionService.requestCounts$,
          this.resultSelectionService.requestSizes$,
          this.resultSelectionService.percentages$
        ];
      }
    });

    this.resultSelectionService.heroTimings$.subscribe(next => {
      if(next) {
        this.measurands = [
          this.resultSelectionService.loadTimes$,
          this.resultSelectionService.userTimings$,
          this.resultSelectionService.heroTimings$,
          this.resultSelectionService.requestCounts$,
          this.resultSelectionService.requestSizes$,
          this.resultSelectionService.percentages$
        ];
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
