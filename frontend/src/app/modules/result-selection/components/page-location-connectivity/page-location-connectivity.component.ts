import {Component, Input, OnInit} from '@angular/core';
import {ResultSelectionService} from "../../services/result-selection.service";
import {Observable} from "rxjs";
import {MeasuredEvent} from "../../../../models/measured-event.model";
import {Location} from "../../../../models/location.model";
import {Connectivity} from "../../../../models/connectivity.model";
import {ResultSelectionStore, UiComponent} from "../../services/result-selection.store";
import {ResponseWithLoadingState} from "../../../../models/response-with-loading-state.model";
import {ResultSelectionCommandParameter} from "../../models/result-selection-command.model";

@Component({
  selector: 'osm-result-selection-page-location-connectivity',
  templateUrl: './page-location-connectivity.component.html',
  styleUrls: ['./page-location-connectivity.component.scss']
})
export class PageLocationConnectivityComponent implements OnInit {
  eventsAndPages$: Observable<ResponseWithLoadingState<MeasuredEvent[]>>;
  locationsAndBrowsers$: Observable<ResponseWithLoadingState<Location[]>>;
  connectivities$: Observable<ResponseWithLoadingState<Connectivity[]>>;

  ActiveTab: typeof ActiveTab = ActiveTab;
  activeTab: ActiveTab;
  ResultSelectionCommandParameter = ResultSelectionCommandParameter;

  @Input() showMeasuredStepSelection: boolean = true;
  @Input() showLocationSelection: boolean = true;
  @Input() showPageSelection: boolean = true;
  @Input() showBrowserSelection: boolean = true;
  @Input() showConnectivitySelection: boolean = true;

  constructor(private resultSelectionService: ResultSelectionService, private resultSelectionStore: ResultSelectionStore) {
    this.resultSelectionStore.registerComponent(UiComponent.PAGE_LOCATION_CONNECTIVITY);
    this.eventsAndPages$ = this.resultSelectionStore.eventsAndPages$;
    this.locationsAndBrowsers$ = this.resultSelectionStore.locationsAndBrowsers$;
    this.connectivities$ = this.resultSelectionStore.connectivities$;
  }

  ngOnInit(): void {
    if (this.showPageSelection) {
      this.activeTab = ActiveTab.PageAndEvent;
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
