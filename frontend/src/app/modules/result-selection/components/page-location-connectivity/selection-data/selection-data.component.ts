import {Component, Input, OnInit} from '@angular/core';
import {BehaviorSubject, combineLatest, Observable} from "rxjs";
import {Location} from "../../../../../models/location.model";
import {Page} from "../../../../../models/page.model";
import {map} from "rxjs/operators";
import {MeasuredEvent} from "../../../../../models/measured-event.model";
import {Browser} from "../../../../../models/browser.model";
import {Connectivity} from "../../../../../models/connectivity.model";
import {ResponseWithLoadingState} from "../../../../../models/response-with-loading-state.model";
import {ResultSelectionStore} from "../../../services/result-selection.store";
import {ResultSelectionCommandParameter} from "../../../models/result-selection-command.model";

@Component({
  selector: 'osm-selection-data',
  templateUrl: './selection-data.component.html',
  styleUrls: ['./selection-data.component.scss']
})
export class SelectionDataComponent implements OnInit {
  @Input() parentChildData$: Observable<ResponseWithLoadingState<(Location | MeasuredEvent)[]>>;
  childData$: Observable<(Location | MeasuredEvent)[]>;
  uniqueParents$: Observable<(Browser | Page | Connectivity)[]>;

  @Input() childType: ResultSelectionCommandParameter;
  @Input() parentType: ResultSelectionCommandParameter;
  @Input() showChildSelection: boolean = true;
  @Input() parentRequired: boolean = false;

  parentSelection$ = new BehaviorSubject<number[]>([]);
  parentSelection: number[] = [];
  childSelection: number[] = [];

  constructor(private resultSelectionStore: ResultSelectionStore) {
  }

  ngOnInit(): void {
    this.childData$ = this.getChildData();
    this.uniqueParents$ = this.getUniqueParents();
    this.resultSelectionStore.reset$.subscribe(() => this.resetResultSelection());
    this.handleQueryParams();
  }

  filterSelectableItems(selectedParents: number[]): void {
    this.parentSelection$.next(selectedParents);
    this.resultSelectionStore.setResultSelectionCommandIds(selectedParents, this.parentType);
  }

  updateResultSelectionCommand(): void {
    this.resultSelectionStore.setResultSelectionCommandIds(this.childSelection, this.childType);
  }

  determineOpacity(selectionLength: number): number {
    if (this.parentRequired || selectionLength > 0) {
      return 1;
    } else {
      return 0.5;
    }
  }

  private getChildData(): Observable<(Location | MeasuredEvent)[]> {
    return combineLatest(this.parentChildData$, this.parentSelection$).pipe(
      map(([parentChildData, selectedParents]: [ResponseWithLoadingState<(Location | MeasuredEvent)[]>, number[]]) => {
        let selectableData: (Location | MeasuredEvent)[] = parentChildData.data;
        if (selectedParents && selectedParents.length) {
          selectableData = selectableData.filter(item => selectedParents.includes(item.parent.id));
        }
        this.childSelection = selectableData.filter(item => this.childSelection.includes(item.id)).map(item => item.id);
        return this.sortAlphabetically(selectableData);
      })
    );
  }

  private getUniqueParents(): Observable<(Browser | Page | Connectivity)[]> {
    return this.parentChildData$.pipe(
      map((next: ResponseWithLoadingState<(Location | MeasuredEvent)[]>) => {
        if (this.parentType !== ResultSelectionCommandParameter.CONNECTIVITIES) {
          let parents: (Browser | Page)[] = next.data.map(value => value.parent);
          let uniqueParents: (Browser | Page)[] = this.getUniqueElements(parents);
          return this.sortAlphabetically(uniqueParents);
        } else {
          return this.sortAlphabetically(next.data);
        }
      })
    );
  }

  private resetResultSelection() {
    if (this.parentSelection.length > 0) {
      this.parentSelection$.next([]);
      this.parentSelection = [];
    }
    if (this.showChildSelection && this.childSelection.length > 0) {
      this.childSelection = [];
    }
  }

  private getUniqueElements(items: (Browser | Page)[]): (Browser | Page)[] {
    let map = new Map();
    let parentElements = [];
    for (let item of items) {
      if (!map.has(item.id)) {
        map.set(item.id, item.name);
        parentElements.push({
          id: item.id,
          name: item.name
        })
      }
    }
    return parentElements;
  }

  private sortAlphabetically<T extends { name: string }>(items: T[]): T[] {
    return items.sort((a, b) => {
      return a.name.localeCompare(b.name);
    })
  }

  private handleQueryParams(): void {
    if (this.resultSelectionStore.resultSelectionCommand[this.parentType]) {
      this.parentSelection = this.resultSelectionStore.resultSelectionCommand[this.parentType];
      if (this.showChildSelection && this.resultSelectionStore.resultSelectionCommand[this.childType]) {
        this.childSelection = this.resultSelectionStore.resultSelectionCommand[this.childType];
      }
    }
  }
}
