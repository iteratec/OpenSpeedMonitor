import {Component, Input} from '@angular/core';
import {ResultSelectionService} from "../../services/result-selection.service";
import {Caller} from "../../models/result-selection-command.model";
import {Chart} from "../../models/chart.model";
import {Observable, of} from "rxjs";
import {MeasuredEvent} from "../../../../models/measured-event.model";
import {Location} from "../../../../models/location.model";
import {Connectivity} from "../../../../models/connectivity.model";
import {Page} from "../../../../models/page.model";
import {Browser} from "../../../../models/browser.model";

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
  browserAndLocationSelectionActive: boolean;
  connectivitySelectionActive: boolean;

  uniquePages$: Observable<Page[]>;
  uniqueBrowsers$: Observable<Browser[]>;
  locations$: Observable<Location[]>;
  measuredEvents$: Observable<MeasuredEvent[]>;

  selectedPages: number[];
  selectedEvents: number[];
  selectableEvents: number[];
  allEventsSelected: boolean = true;

  selectedBrowsers: number[];
  selectedLocations: number[];
  selectableBrowsers: number[];
  selectableLocations: number[];
  allLocationsSelected: boolean = true;
  allBrowsersSelected: boolean = true;

  selectedConnectivities: number[];
  selectableConnectivities: number[];
  allConnectivitiesSelected: boolean = true;

  @Input() currentChart: string;

  constructor(private resultSelectionService: ResultSelectionService) {
    this.eventsAndPages$ = this.resultSelectionService.eventsAndPages$;
    this.locationsAndBrowsers$ = this.resultSelectionService.locationsAndBrowsers$;
    this.connectivities$ = this.resultSelectionService.connectivities$;

    let defaultFrom = new Date();
    let defaultTo = new Date();
    defaultFrom.setDate(defaultTo.getDate() - 7);
    const testResultSelectionCommand = {
      from: defaultFrom,
      to: defaultTo,
      caller: Caller.EventResult,
      jobGroupIds: [],
      pageIds: [],
      locationIds: [],
      browserIds: [],
      measuredEventIds: [],
      selectedConnectivities: []
    };
    this.resultSelectionService.loadSelectableData(testResultSelectionCommand, Chart.TimeSeries);

    this.eventsAndPages$.subscribe(next => {
      if (next) {
        this.measuredEvents$ = of(this.sortAlphabetically(next));
        this.selectableEvents = next.map(value => value.id);
        let pages: Page[] = next.map(value => value.parent);
        let uniquePages: Page[] = this.getUniqueElements(pages);
        this.uniquePages$ = of(uniquePages);
      }
    });

    this.locationsAndBrowsers$.subscribe(next => {
      if (next) {
        this.locations$ = of(this.sortAlphabetically(next));
        this.selectableLocations = next.map(value => value.id);
        let browsers: Browser[] = next.map(value => value.parent);
        let uniqueBrowsers: Browser[] = this.getUniqueElements(browsers);
        this.selectableBrowsers = uniqueBrowsers.map(value => value.id);
        this.uniqueBrowsers$ = of(uniqueBrowsers);
      }
    });

    this.connectivities$.subscribe(next => {
      if (next) {
        this.selectableConnectivities = next.map(value => value.id);
      }
    })
  }

  switchTab(clickedTab: HTMLLIElement): void {
    clickedTab.classList.add('active');
    if (clickedTab.id === 'browserAndLocationTab') {
      this.pageAndEventSelectionActive = false;
      this.browserAndLocationSelectionActive = true;
      this.connectivitySelectionActive = false;
    } else if (clickedTab.id === 'connectivityTab') {
      this.pageAndEventSelectionActive = false;
      this.browserAndLocationSelectionActive = false;
      this.connectivitySelectionActive = true;
    } else {
      this.pageAndEventSelectionActive = true;
      this.browserAndLocationSelectionActive = false;
      this.connectivitySelectionActive = false;
    }
  }

  selectAllItems(selectAllCheckbox: HTMLInputElement): void {
    if (selectAllCheckbox.id === 'selectAllEventsCheckbox') {
      if (this.selectedEvents && this.selectedEvents.length > 0) {
        this.selectedEvents = [];
        this.allEventsSelected = true;
        selectAllCheckbox.checked = true;
      }
    } else if (selectAllCheckbox.id === 'selectAllLocationsCheckbox') {
      if (this.selectedLocations && this.selectedLocations.length > 0) {
        this.selectedLocations = [];
        this.allLocationsSelected = true;
        selectAllCheckbox.checked = true;
      }
    } else if (selectAllCheckbox.id === 'selectAllBrowsersCheckbox') {
      if (this.selectedBrowsers && this.selectedBrowsers.length > 0) {
        this.selectedBrowsers = [];
        this.allBrowsersSelected = true;
        selectAllCheckbox.checked = true;
        document.querySelector('#result-selection-browser-selection').attributes.getNamedItem('style').value = 'opacity: 0.5;';
      }
    } else {
      if (this.selectedConnectivities && this.selectedConnectivities.length > 0) {
        this.selectedConnectivities = [];
        this.allConnectivitiesSelected = true;
        selectAllCheckbox.checked = true;
        document.querySelector('#result-selection-connectivity-selection').attributes.getNamedItem('style').value = 'opacity: 0.5;';
      }
    }
  }

  changedSelection(selectAllCheckbox: HTMLInputElement): void {
    if (selectAllCheckbox.id === 'selectAllEventsCheckbox') {
      if (this.selectedEvents && this.selectedEvents.length > 0) {
        this.allEventsSelected = false;
        selectAllCheckbox.checked = false;
      } else {
        this.allEventsSelected = true;
        selectAllCheckbox.checked = true;
      }
    } else if (selectAllCheckbox.id === 'selectAllLocationsCheckbox') {
      if (this.selectedLocations && this.selectedLocations.length > 0) {
        this.allLocationsSelected = false;
        selectAllCheckbox.checked = false;
      } else {
        this.allLocationsSelected = true;
        selectAllCheckbox.checked = true;
      }
    } else if (selectAllCheckbox.id === 'selectAllBrowsersCheckbox') {
      if (this.selectedBrowsers && this.selectedBrowsers.length > 0) {
        this.allBrowsersSelected = false;
        selectAllCheckbox.checked = false;
        document.querySelector('#result-selection-browser-selection').attributes.getNamedItem('style').value = 'opacity: 1;';
      } else {
        this.allBrowsersSelected = true;
        selectAllCheckbox.checked = true;
        document.querySelector('#result-selection-browser-selection').attributes.getNamedItem('style').value = 'opacity: 0.5;';
      }
    } else {
      if (this.selectedConnectivities && this.selectedConnectivities.length > 0) {
        this.allConnectivitiesSelected = false;
        selectAllCheckbox.checked = false;
        document.querySelector('#result-selection-connectivity-selection').attributes.getNamedItem('style').value = 'opacity: 1;';
      } else {
        this.allConnectivitiesSelected = true;
        selectAllCheckbox.checked = true;
        document.querySelector('#result-selection-connectivity-selection').attributes.getNamedItem('style').value = 'opacity: 0.5;';
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
