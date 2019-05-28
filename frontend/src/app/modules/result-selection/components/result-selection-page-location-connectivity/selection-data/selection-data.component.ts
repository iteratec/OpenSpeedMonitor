import {Component, Input, OnInit} from '@angular/core';
import {BehaviorSubject, combineLatest, Observable} from "rxjs";
import {Location} from "../../../../../models/location.model";
import {Page} from "../../../../../models/page.model";
import {map} from "rxjs/operators";
import {MeasuredEvent} from "../../../../../models/measured-event.model";
import {Browser} from "../../../../../models/browser.model";
import {Connectivity} from "../../../../../models/connectivity.model";

@Component({
  selector: 'osm-selection-data',
  templateUrl: './selection-data.component.html',
  styleUrls: ['./selection-data.component.scss']
})
export class SelectionDataComponent implements OnInit {
  @Input() parentChildData$: Observable<(Location | MeasuredEvent)[]>;
  childData$: Observable<(Location | MeasuredEvent)[]>;
  uniqueParents$: Observable<(Browser | Page | Connectivity)[]>;

  @Input() childType: string;
  @Input() parentType: string;
  @Input() showChildSelection: boolean = true;
  @Input() parentSelectionOptional: boolean = true;

  parentSelection$ = new BehaviorSubject<number[]>([]);
  parentSelection: number[] = [];
  childSelection: number[] = [];

  constructor() { }

  ngOnInit(): void {
    this.childData$ = combineLatest(this.parentChildData$, this.parentSelection$).pipe(
      map(([parentChildData, selectedParents]) => {
        if (selectedParents && selectedParents.length) {
          parentChildData = parentChildData.filter(item => selectedParents.includes(item.parent.id));
          this.childSelection = parentChildData.filter(item => this.childSelection.includes(item.id)).map(item => item.id);
        }
        return this.sortAlphabetically(parentChildData);
      })
    );

    this.uniqueParents$ = this.parentChildData$.pipe(
      map(next => {
        if (this.parentType !== 'connectivity') {
          let parents: (Browser | Page)[] = next.map(value => value.parent);
          let uniqueParents: (Browser | Page)[] = this.getUniqueElements(parents);
          return this.sortAlphabetically(uniqueParents);
        } else {
          return this.sortAlphabetically(next);
        }
      })
    )
  }

  filterSelectableItems(selectedParents: number[]): void {
    this.parentSelection$.next(selectedParents);
  }

  determineOpacity(selectionLength: number, parentSelectionOptional: boolean): number {
    if (!parentSelectionOptional) {
      return 1;
    } else if (selectionLength > 0) {
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
