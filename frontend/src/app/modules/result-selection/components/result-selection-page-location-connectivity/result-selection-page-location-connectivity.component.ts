import {Component, Input, OnInit} from '@angular/core';
import {ResultSelectionService} from "../../services/result-selection.service";
import {Observable} from "rxjs";
import {MeasuredEvent} from "../../../../models/measured-event.model";
import {Location} from "../../../../models/location.model";
import {Connectivity} from "../../../../models/connectivity.model";

@Component({
  selector: 'osm-result-selection-page-location-connectivity',
  templateUrl: './result-selection-page-location-connectivity.component.html',
  styleUrls: ['./result-selection-page-location-connectivity.component.scss']
})
export class ResultSelectionPageLocationConnectivityComponent implements OnInit {
  eventsAndPages$: Observable<MeasuredEvent[]>;
  locationsAndBrowsers$: Observable<Location[]>;
  connectivities$: Observable<Connectivity[]>;

  ActiveTab: typeof ActiveTab = ActiveTab;
  activeTab: ActiveTab;

  @Input() showMeasuredStepSelection: boolean = true;
  @Input() showLocationSelection: boolean = true;
  @Input() showPageSelection: boolean = true;
  @Input() showBrowserSelection: boolean = true;
  @Input() showConnectivitySelection: boolean = true;

  constructor(private resultSelectionService: ResultSelectionService) {
    this.eventsAndPages$ = this.resultSelectionService.eventsAndPages$;
    this.locationsAndBrowsers$ = this.resultSelectionService.locationsAndBrowsers$;
    this.connectivities$ = this.resultSelectionService.connectivities$;
  }

  ngOnInit(): void {
    if (this.showPageSelection) {
      this.activeTab = ActiveTab.PageAndEvent
    }
    else if (!this.showPageSelection && this.showBrowserSelection) {
      this.activeTab = ActiveTab.BrowserAndLocation
    }
    else {
      this.activeTab = ActiveTab.Connectivity
    }
  }
}

export enum ActiveTab {
  PageAndEvent,
  BrowserAndLocation,
  Connectivity
}
