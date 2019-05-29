import {Component, Input} from '@angular/core';
import {ResultSelectionService} from "../../services/result-selection.service";
import {Observable, of} from "rxjs";
import {MeasuredEvent} from "../../../../models/measured-event.model";
import {Location} from "../../../../models/location.model";
import {Connectivity} from "../../../../models/connectivity.model";
import {Page} from "../../../../models/page.model";
import {Browser} from "../../../../models/browser.model";
import {ResultSelectionStore, UiComponent} from "../../services/result-selection.store";
import {ResultSelectionCommandParameter} from "../../models/result-selection-command.model";
import {ResponseWithLoadingState} from "../../../../models/response-with-loading-state.model";

@Component({
  selector: 'osm-result-selection-page-location-connectivity',
  templateUrl: './page-location-connectivity.component.html',
  styleUrls: ['./page-location-connectivity.component.scss']
})
export class PageLocationConnectivityComponent {
  eventsAndPages$: Observable<ResponseWithLoadingState<MeasuredEvent[]>>;
  locationsAndBrowsers$: Observable<ResponseWithLoadingState<Location[]>>;
  connectivities$: Observable<ResponseWithLoadingState<Connectivity[]>>;

  pageAndEventSelectionActive: boolean = true;
  browserAndLocationSelectionActive: boolean = false;
  connectivitySelectionActive: boolean = false;

  uniquePages$: Observable<Page[]>;
  uniqueBrowsers$: Observable<Browser[]>;
  locations$: Observable<Location[]>;
  measuredEvents$: Observable<MeasuredEvent[]>;

  selectedPages: number[] = [];
  selectedEvents: number[] = [];
  selectedBrowsers: number[] = [];
  selectedLocations: number[] = [];
  selectedConnectivities: number[] = [];

  @Input() currentChart: string;
  @Input() showMeasuredStepSelection: boolean = true;
  @Input() showLocationSelection: boolean = true;
  @Input() showOnlyPageSelection: boolean = false;
  @Input() showOnlyBrowserSelection: boolean = false;

  constructor(private resultSelectionService: ResultSelectionService, private resultSelectionStore: ResultSelectionStore) {
    this.resultSelectionStore.registerComponent(UiComponent.PAGE_LOCATION_CONNECTIVITY);
    this.eventsAndPages$ = this.resultSelectionStore.eventsAndPages$;
    this.locationsAndBrowsers$ = this.resultSelectionStore.locationsAndBrowsers$;
    this.connectivities$ = this.resultSelectionStore.connectivities$;

    this.eventsAndPages$.subscribe(next => {
      if (next && next.data) {
        this.measuredEvents$ = of(this.sortAlphabetically(next.data));
        let pages: Page[] = next.data.map(value => value.parent);
        let uniquePages: Page[] = this.getUniqueElements(pages);
        this.uniquePages$ = of(this.sortAlphabetically(uniquePages));
      }
    });

    this.locationsAndBrowsers$.subscribe(next => {
      if (next && next.data) {
        this.locations$ = of(this.sortAlphabetically(next.data));
        let browsers: Browser[] = next.data.map(value => value.parent);
        let uniqueBrowsers: Browser[] = this.getUniqueElements(browsers);
        this.uniqueBrowsers$ = of(this.sortAlphabetically(uniqueBrowsers));
      }
    });
  }

  filterSelectableItems(selectedParents: number[], children: String): void {
    let items = [];
    if (this.showMeasuredStepSelection && children === 'events') {
      items = this.resultSelectionStore.eventsAndPages$.getValue().data;
    } else if (this.showLocationSelection && children === 'locations') {
      items = this.resultSelectionStore.locationsAndBrowsers$.getValue().data;
    }

    if (selectedParents && selectedParents.length > 0) {
      let filteredItems = items.filter(item => selectedParents.includes(item.parent.id));
      if (children === 'events') {
        this.selectedEvents = filteredItems.filter(item => this.selectedEvents.includes(item.id)).map(item => item.id);
        this.resultSelectionStore.setResultSelectionCommandIds(this.selectedPages, ResultSelectionCommandParameter.PAGES);
        this.resultSelectionStore.setResultSelectionCommandIds(this.selectedEvents, ResultSelectionCommandParameter.MEASURED_EVENTS);
        this.measuredEvents$ = of(this.sortAlphabetically(filteredItems));
      } else if (children === 'locations') {
        this.selectedLocations = filteredItems.filter(item => this.selectedLocations.includes(item.id)).map(item => item.id);
        this.resultSelectionStore.setResultSelectionCommandIds(this.selectedBrowsers, ResultSelectionCommandParameter.BROWSERS);
        this.resultSelectionStore.setResultSelectionCommandIds(this.selectedLocations, ResultSelectionCommandParameter.LOCATIONS);
        this.locations$ = of(this.sortAlphabetically(filteredItems));
      }
    } else {
      if (children === 'events') {
        this.measuredEvents$ = of(this.sortAlphabetically(items));
        this.resultSelectionStore.setResultSelectionCommandIds(this.selectedPages, ResultSelectionCommandParameter.PAGES);
        this.resultSelectionStore.setResultSelectionCommandIds(this.selectedEvents, ResultSelectionCommandParameter.MEASURED_EVENTS);
      } else if (children === 'locations') {
        this.locations$ = of(this.sortAlphabetically(items));
        this.resultSelectionStore.setResultSelectionCommandIds(this.selectedBrowsers, ResultSelectionCommandParameter.BROWSERS);
        this.resultSelectionStore.setResultSelectionCommandIds(this.selectedLocations, ResultSelectionCommandParameter.LOCATIONS);
      }
    }
  }

  private getUniqueElements(items) {
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

  private sortAlphabetically(items) {
    return items.sort((a, b) => {
      return a.name.localeCompare(b.name);
    })
  }
}
