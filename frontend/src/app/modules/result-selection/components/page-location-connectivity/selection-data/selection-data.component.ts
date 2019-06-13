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
  @Input() parentSelectionOptional: boolean = true;

  parentSelection$ = new BehaviorSubject<number[]>([]);
  parentSelection: number[] = [];
  childSelection: number[] = [];

  constructor(private resultSelectionStore: ResultSelectionStore) {
  }

  ngOnInit(): void {
    this.childData$ = combineLatest(this.parentChildData$, this.parentSelection$).pipe(
      map(([parentChildData, selectedParents]: [ResponseWithLoadingState<(Location | MeasuredEvent)[]>, number[]]) => {
        let selectableData: (Location | MeasuredEvent)[] = parentChildData.data;
        if (selectedParents && selectedParents.length) {
          selectableData = selectableData.filter(item => selectedParents.includes(item.parent.id));
          this.childSelection = selectableData.filter(item => this.childSelection.includes(item.id)).map(item => item.id);
        }
        return this.sortAlphabetically(selectableData);
      })
    );

    this.uniqueParents$ = this.parentChildData$.pipe(
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

  filterSelectableItems(selectedParents: number[]): void {
    this.parentSelection$.next(selectedParents);
    this.resultSelectionStore.setResultSelectionCommandIds(selectedParents, this.parentType);
  }

  updateResultSelectionCommand(): void {
    this.resultSelectionStore.setResultSelectionCommandIds(this.childSelection, this.childType);
  }

  determineOpacity(selectionLength: number, parentSelectionOptional: boolean): number {
    if (!parentSelectionOptional || selectionLength > 0) {
      return 1;
    } else {
      return 0.5;
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
}
