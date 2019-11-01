import {Component, OnInit} from '@angular/core';
import {URL} from "../../enums/url.enum";
import {LinechartDataService} from "./services/linechart-data.service";
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {EventResultData, EventResultDataDTO} from './models/event-result-data.model';
import {EventDTO} from './models/event.model';
import {BehaviorSubject, forkJoin} from 'rxjs';

@Component({
  selector: 'osm-time-series',
  templateUrl: './time-series.component.html',
  styleUrls: ['./time-series.component.scss']
})
export class TimeSeriesComponent implements OnInit {

  public results$ = new BehaviorSubject<EventResultDataDTO>(new EventResultData());

  constructor(private linechartDataService: LinechartDataService, private resultSelectionStore: ResultSelectionStore) { }

  ngOnInit() {
  }

  getTimeSeriesChartData() {
    return this.linechartDataService.fetchEventResultData<EventResultDataDTO>(
      this.resultSelectionStore.resultSelectionCommand,
      this.resultSelectionStore.remainingResultSelection,
      URL.EVENT_RESULT_DASHBOARD_LINECHART_DATA
    );
  }

  getEvents() {
    return this.linechartDataService.fetchEvents<EventDTO[]>(
      this.resultSelectionStore.resultSelectionCommand,
      URL.EVENTS
    );
  }

  getData() {
    forkJoin({
      eventResultData: this.getTimeSeriesChartData(),
      events: this.getEvents()
    })
    .subscribe((next) => {
      next.eventResultData.events = next.events;
      this.results$.next(next.eventResultData);
    });
  }
}
