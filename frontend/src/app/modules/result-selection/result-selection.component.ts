import { Component, OnInit } from '@angular/core';
import {ResultSelectionService} from "./services/result-selection.service";
import {Observable} from "rxjs";
import {SelectableApplication} from "./models/selectable-application.model";
import {SelectableMeasuredEvent} from "./models/selectable-measured-event.model";
import {SelectableLocation} from "./models/selectable-location.model";
import {SelectableConnectivity} from "./models/selectable-connectivity.model";
import {SelectableHeroTiming} from "./models/selectable-hero-timing.model";
import {SelectableUserTiming} from "./models/selectable-user-timing.model";

@Component({
  selector: 'osm-result-selection',
  templateUrl: './result-selection.component.html',
  styleUrls: ['./result-selection.component.scss']
})
export class ResultSelectionComponent implements OnInit {

  selectableApplications$: Observable<SelectableApplication[]>;
  selectableEventsAndPages$: Observable<SelectableMeasuredEvent[]>;
  selectableLocationsAndBrowsers$: Observable<SelectableLocation[]>;
  selectableConnectivities$: Observable<SelectableConnectivity[]>;
  selectableHeroTimings$: Observable<SelectableHeroTiming[]>;
  selectableUserTimings$: Observable<SelectableUserTiming[]>;
  resultCount$: Observable<string>;

  constructor(private resultSelectionService: ResultSelectionService) {

  }

  ngOnInit() {
  }

}
