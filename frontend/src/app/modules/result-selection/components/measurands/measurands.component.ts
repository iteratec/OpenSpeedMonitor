import {Component, OnInit} from '@angular/core';
import {MeasurandGroup, SelectableMeasurand} from "../../../../models/measurand.model";
import {BehaviorSubject, combineLatest, Observable} from "rxjs";
import {ResultSelectionStore} from "../../services/result-selection.store";
import {ResponseWithLoadingState} from "../../../../models/response-with-loading-state.model";
import {map} from 'rxjs/operators';
import {UiComponent} from "../../../../enums/ui-component.enum";

@Component({
  selector: 'osm-result-selection-measurands',
  templateUrl: './measurands.component.html',
  styleUrls: ['./measurands.component.scss']
})
export class MeasurandsComponent implements OnInit {
  measurands$: BehaviorSubject<ResponseWithLoadingState<BehaviorSubject<MeasurandGroup>[]>> = new BehaviorSubject({
    isLoading: false,
    data: []
  });

  selectedMeasurands: SelectableMeasurand[] = [];
  defaultValue: SelectableMeasurand;

  constructor(private resultSelectionStore: ResultSelectionStore) {
    this.measurands$.next({
      ...this.measurands$.getValue(),
      data: [
        this.resultSelectionStore.loadTimes$,
        this.resultSelectionStore.userTimings$,
        this.resultSelectionStore.heroTimings$,
        this.resultSelectionStore.requestCounts$,
        this.resultSelectionStore.requestSizes$,
        this.resultSelectionStore.percentages$
      ]
    });
    this.setDefaultValue();
  }

  ngOnInit() {
    this.resultSelectionStore.registerComponent(UiComponent.MEASURAND);
    this.loadingState().subscribe(next => {
      this.measurands$.next({...this.measurands$.getValue(), isLoading: next});
    });
  }

  selectMeasurand(index: number, measurand: SelectableMeasurand) {
    this.selectedMeasurands[index] = measurand;
    this.setMeasurandIds();
  }

  addMeasurandField() {
    this.selectedMeasurands.push(this.defaultValue);
    this.setMeasurandIds();
  }

  removeMeasurandField(index: number) {
    this.selectedMeasurands.splice(index, 1);
    this.setMeasurandIds();
  }

  trackByFn(index: number, item: any) {
    return index;
  }

  setDefaultValue() {
    this.resultSelectionStore.loadTimes$.subscribe((next: MeasurandGroup) => {
      this.defaultValue = next.values[0];
      this.selectedMeasurands = [this.defaultValue];
      if (this.defaultValue) {
        this.setMeasurandIds();
      }
    });
  }

  private loadingState(): Observable<boolean> {
    return combineLatest(
      this.resultSelectionStore.loadTimes$,
      this.resultSelectionStore.userTimings$,
      this.resultSelectionStore.heroTimings$,
      this.resultSelectionStore.requestCounts$,
      this.resultSelectionStore.requestSizes$,
      this.resultSelectionStore.percentages$
    ).pipe(
        map(next => next.map(item => item.isLoading).some(value => value))
    )
  }

  private setMeasurandIds() {
    this.resultSelectionStore.setMeasurands(
      this.selectedMeasurands.map((measurand: SelectableMeasurand) => measurand.id));
  }
}
