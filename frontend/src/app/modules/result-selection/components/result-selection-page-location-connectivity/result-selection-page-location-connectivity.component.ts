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

  constructor(private resultSelectionService: ResultSelectionService) {
    this.eventsAndPages$ = this.resultSelectionService.eventsAndPages$;
    this.locationsAndBrowsers$ = this.resultSelectionService.locationsAndBrowsers$;
    this.connectivities$ = this.resultSelectionService.connectivities$;

    this.eventsAndPages$.subscribe(next => {
      if (next) {
        this.measuredEvents$ = of(this.sortAlphabetically(next));
        let pages: Page[] = next.map(value => value.parent);
        let uniquePages: Page[] = this.getUniqueElements(pages);
        this.uniquePages$ = of(uniquePages);
      }
    });

    this.locationsAndBrowsers$.subscribe(next => {
      if (next) {
        this.locations$ = of(this.sortAlphabetically(next));
        let browsers: Browser[] = next.map(value => value.parent);
        let uniqueBrowsers: Browser[] = this.getUniqueElements(browsers);
        this.uniqueBrowsers$ = of(uniqueBrowsers);
      }
    });
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
