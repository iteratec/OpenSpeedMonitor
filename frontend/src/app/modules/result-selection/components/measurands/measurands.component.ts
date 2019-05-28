import {Component, OnInit} from '@angular/core';
import {MeasurandGroup, SelectableMeasurand} from "../../../../models/measurand.model";
import {BehaviorSubject, Observable} from "rxjs";
import {ResponseWithLoadingState} from "../../../../models/response-with-loading-state.model";
import {ResultSelectionStore, UiComponent} from "../../services/result-selection.store";

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

  measurands: BehaviorSubject<ResponseWithLoadingState<MeasurandGroup>>[];

  selectedMeasurands: SelectableMeasurand[] = [];
  defaultValue: SelectableMeasurand;

  constructor(private resultSelectionStore: ResultSelectionStore) {
    this.resultSelectionStore.registerComponent(UiComponent.MEASURAND);

    this.measurands = [
      this.resultSelectionStore.loadTimes$,
      this.resultSelectionStore.userTimings$,
      this.resultSelectionStore.heroTimings$,
      this.resultSelectionStore.requestCounts$,
      this.resultSelectionStore.requestSizes$,
      this.resultSelectionStore.percentages$
    ];

    this.resultSelectionStore.loadTimes$.subscribe(next => {
      if(next) {
        this.defaultValue = next.data.values[0];
        this.selectedMeasurands = [this.defaultValue];
      }
    });

    this.resultSelectionStore.userTimings$.subscribe(next => {
      if(next) {
        this.measurands = [
          this.resultSelectionStore.loadTimes$,
          this.resultSelectionStore.userTimings$,
          this.resultSelectionStore.heroTimings$,
          this.resultSelectionStore.requestCounts$,
          this.resultSelectionStore.requestSizes$,
          this.resultSelectionStore.percentages$
        ];
      }
    });

    this.resultSelectionStore.heroTimings$.subscribe(next => {
      if(next) {
        this.measurands = [
          this.resultSelectionStore.loadTimes$,
          this.resultSelectionStore.userTimings$,
          this.resultSelectionStore.heroTimings$,
          this.resultSelectionStore.requestCounts$,
          this.resultSelectionStore.requestSizes$,
          this.resultSelectionStore.percentages$
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
