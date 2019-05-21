import {Component, Input} from '@angular/core';
import {ResultSelectionService} from "../../services/result-selection.service";
import {Observable, of} from "rxjs";
import {MeasuredEvent} from "../../../../models/measured-event.model";
import {Location} from "../../../../models/location.model";
import {Connectivity} from "../../../../models/connectivity.model";
import {Page} from "../../../../models/page.model";
import {Browser} from "../../../../models/browser.model";
import {ResultSelectionStore} from "../../services/result-selection.store";

@Component({
  selector: 'osm-result-selection-page-location-connectivity',
  templateUrl: './result-selection-page-location-connectivity.component.html',
  styleUrls: ['./result-selection-page-location-connectivity.component.scss']
})
export class ResultSelectionPageLocationConnectivityComponent {
  eventsAndPages$: Observable<MeasuredEvent[]>;
  locationsAndBrowsers$: Observable<Location[]>;
  connectivities$: Observable<Connectivity[]>;

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
  selectedComponent: string = "PAGE_LOCATION_CONNECTIVITY";

  @Input() currentChart: string;
  @Input() showMeasuredStepSelection: boolean = true;
  @Input() showLocationSelection: boolean = true;
  @Input() showOnlyPageSelection: boolean = false;
  @Input() showOnlyBrowserSelection: boolean = false;

  constructor(private resultSelectionService: ResultSelectionService, private resultSelectionStore: ResultSelectionStore) {
    this.resultSelectionStore.resultSelectionCommandListener(this.selectedComponent);
    this.eventsAndPages$ = this.resultSelectionStore.eventsAndPages$;
    this.locationsAndBrowsers$ = this.resultSelectionStore.locationsAndBrowsers$;
    this.connectivities$ = this.resultSelectionStore.connectivities$;

    this.eventsAndPages$.subscribe(next => {
      if (next) {
        this.measuredEvents$ = of(this.sortAlphabetically(next));
        let pages: Page[] = next.map(value => value.parent);
        let uniquePages: Page[] = this.getUniqueElements(pages);
        this.uniquePages$ = of(this.sortAlphabetically(uniquePages));
      }
    });

    this.locationsAndBrowsers$.subscribe(next => {
      if (next) {
        this.locations$ = of(this.sortAlphabetically(next));
        let browsers: Browser[] = next.map(value => value.parent);
        let uniqueBrowsers: Browser[] = this.getUniqueElements(browsers);
        this.uniqueBrowsers$ = of(this.sortAlphabetically(uniqueBrowsers));
      }
    });
  }

  filterSelectableItems(selectedParents: number[], children: String): void {
    let items = [];
    if (this.showMeasuredStepSelection && children === 'events') {
      items = this.resultSelectionService.eventsAndPages$.getValue();
    } else if (this.showLocationSelection && children === 'locations') {
      items = this.resultSelectionService.locationsAndBrowsers$.getValue();
    }

    if (selectedParents && selectedParents.length > 0) {
      let filteredItems = items.filter(item => selectedParents.includes(item.parent.id));
      if (children === 'events') {
        console.log(this.selectedEvents);
        this.selectedEvents = filteredItems.filter(item => this.selectedEvents.includes(item.id)).map(item => item.id);
        this.measuredEvents$ = of(this.sortAlphabetically(filteredItems));
      } else if (children === 'locations') {
        this.selectedLocations = filteredItems.filter(item => this.selectedLocations.includes(item.id)).map(item => item.id);

        this.locations$ = of(this.sortAlphabetically(filteredItems));
      }
    } else {
      if (children === 'events') {
        this.measuredEvents$ = of(this.sortAlphabetically(items));
      } else if (children === 'locations') {
        this.locations$ = of(this.sortAlphabetically(items));
      }
    }
    //TODO: measurand event and location selection not working yet
    this.resultSelectionStore.setSelectedPages(this.selectedPages);
    this.resultSelectionStore.setSelectedLocations(this.selectedLocations);
    this.resultSelectionStore.setSelectedBrowser(this.selectedBrowsers);
    this.resultSelectionStore.setSelectedConnectivities(this.selectedConnectivities);
    this.resultSelectionStore.setSelectedMeasuredEvents(this.selectedEvents);
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
