import {Component, Input, OnInit} from '@angular/core';
import {MeasurandGroup, SelectableMeasurand} from "../../../../models/measurand.model";
import {BehaviorSubject, combineLatest, Observable} from "rxjs";
import {ResultSelectionStore} from "../../services/result-selection.store";
import {ResponseWithLoadingState} from "../../../../models/response-with-loading-state.model";
import {map} from 'rxjs/operators';
import {UiComponent} from "../../../../enums/ui-component.enum";
import {PerformanceAspectService} from "../../../../services/performance-aspect.service";
import {PerformanceAspectType} from "../../../../models/perfomance-aspect.model";
import {RemainingResultSelectionParameter} from "../../models/remaing-result-selection.model";

@Component({
  selector: 'osm-result-selection-measurands',
  templateUrl: './measurands.component.html',
  styleUrls: ['./measurands.component.scss']
})
export class MeasurandsComponent implements OnInit {
  aspectTypes$:BehaviorSubject<PerformanceAspectType[]> = new BehaviorSubject<PerformanceAspectType[]>([]);
  measurands$: BehaviorSubject<ResponseWithLoadingState<BehaviorSubject<MeasurandGroup>[]>> = new BehaviorSubject({
    isLoading: false,
    data: []
  });

  selectedMeasurands: (PerformanceAspectType | SelectableMeasurand)[] = [];
  defaultValue: PerformanceAspectType | SelectableMeasurand;
  addingComparativeTimeFrameDisabled$: BehaviorSubject<boolean> = new BehaviorSubject(false);

  @Input() multipleMeasurands = false;
  @Input() addingMeasurandsDisabled$: BehaviorSubject<boolean> = new BehaviorSubject(false);

  constructor(private resultSelectionStore: ResultSelectionStore, private performanceAspectService: PerformanceAspectService) {
    this.aspectTypes$ = this.performanceAspectService.aspectTypes$;
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

  selectMeasurand(index: number, measurand: PerformanceAspectType | SelectableMeasurand): void {
    this.selectedMeasurands[index] = measurand;
    this.setResultSelection();
  }

  addMeasurandField(): void {
    this.selectedMeasurands.push(this.defaultValue);
    this.setResultSelection();
    this.addingComparativeTimeFrameDisabled$.next(true);
  }

  removeMeasurandField(index: number): void {
    this.selectedMeasurands.splice(index, 1);
    this.setResultSelection();
    if (this.selectedMeasurands.length == 1) {
      this.addingComparativeTimeFrameDisabled$.next(false);
    }
  }

  trackByFn(index: number, item: any): number {
    return index;
  }

  setDefaultValue(): void {
    this.performanceAspectService.aspectTypes$.subscribe((next: PerformanceAspectType[]) => {
      this.defaultValue = next[0];
      this.selectedMeasurands = [this.defaultValue];
      if (this.defaultValue) {
        this.setResultSelection();
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

  private setResultSelection(): void {
    this.resultSelectionStore.setRemainingResultSelectionEnums(
      this.selectedMeasurands.filter((item: PerformanceAspectType | SelectableMeasurand) => {
        return !('id' in item)
      }).map((performanceAspect: PerformanceAspectType) => performanceAspect.name),
      RemainingResultSelectionParameter.PERFORMANCE_ASPECTS
    );
    this.resultSelectionStore.setRemainingResultSelectionEnums(
      this.selectedMeasurands.filter((item: PerformanceAspectType | SelectableMeasurand) => {
        return 'id' in item
      }).map((measurand: SelectableMeasurand) => measurand.id),
      RemainingResultSelectionParameter.MEASURANDS
    );
  }
}
