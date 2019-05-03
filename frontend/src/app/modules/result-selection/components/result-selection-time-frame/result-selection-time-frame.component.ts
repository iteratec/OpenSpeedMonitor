import {
  Component, Input,
  OnInit,
  ViewChild,
  ViewEncapsulation,
} from '@angular/core';
import {Caller, ResultSelectionCommand} from "../../models/result-selection-command.model";
import {Chart} from "../../models/chart.model";
import {ResultSelectionService} from "../../services/result-selection.service";
import {DateTimeAdapter, OwlDateTimeComponent} from 'ng-pick-datetime';
import {fromEvent, merge, Observable, Subscription} from "rxjs";
import {filter} from "rxjs/operators";
import {OsmLangService} from "../../../../services/osm-lang.service";
import { SharedService } from '../../services/sharedService';

@Component({
  selector: 'osm-result-selection-time-frame',
  templateUrl: './result-selection-time-frame.component.html',
  styleUrls: ['./result-selection-time-frame.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ResultSelectionTimeFrameComponent implements OnInit {

  @Input() currentChart: string;
  @ViewChild('dateTimeFrom') dateTimeFrom: OwlDateTimeComponent<Date>;
  @ViewChild('dateTimeTo') dateTimeTo: OwlDateTimeComponent<Date>;
  @ViewChild('comparativeDateTimeFrom') comparativeDateTimeFrom: OwlDateTimeComponent<Date>;
  @ViewChild('comparativeDateTimeTo') comparativeDateTimeTo: OwlDateTimeComponent<Date>;

  calendarClick$: Observable<MouseEvent>;
  calendarEnter$: Observable<KeyboardEvent>;
  calendarEventSubscription: Subscription;

  selectableTimeFramesInSeconds: number[] = [-1, 3600, 43200, 86400, 259200, 604800, 1209600, 2419200];
  timeFrameInSeconds: number = -1;

  selectableAggregationIntervalsInSeconds: number[] = [-1, 60, 1440, 10080];
  aggregationIntervalInSeconds: number = -1;

  selectedDates: Date[];
  selectedComparativeDates: Date[];
  max = new Date();

  comparativeSelectionActive: boolean = false;

  CalendarType: typeof CalendarType = CalendarType;

  constructor(private resultSelectionService: ResultSelectionService, private dateTimeAdapter: DateTimeAdapter<any>, private osmLangService: OsmLangService, private sharedService: SharedService) {
    if (osmLangService.getOsmLang() == 'en') {
      dateTimeAdapter.setLocale('en-GB');
    } else {
      dateTimeAdapter.setLocale(osmLangService.getOsmLang());
    }
  }

  ngOnInit() {
    let defaultFrom = new Date();
    let defaultTo = new Date();
    defaultTo.setHours(23, 59, 59, 999);
    defaultFrom.setDate(defaultTo.getDate() - 28);
    defaultFrom.setHours(0, 0, 0, 0);

    this.selectedDates = [defaultFrom, defaultTo];
    this.sharedService.change(this.selectedDates);


    let defaultResultSelectionCommand = new ResultSelectionCommand({
      from: defaultFrom,
      to: defaultTo,
      caller: Caller.EventResult,
      jobGroupIds: [],
      pageIds: [],
      locationIds: [],
      browserIds: [],
      measuredEventIds: [],
      selectedConnectivities: []
    });

    this.resultSelectionService.loadSelectableData(defaultResultSelectionCommand, Chart[this.currentChart]);
    this.timeFrameInSeconds = this.selectableTimeFramesInSeconds[7];
  }

  selectTimeFrame(): void {
    if (this.timeFrameInSeconds != this.selectableTimeFramesInSeconds[0]) {
      let from = new Date();
      let to = new Date();
      from.setSeconds(to.getSeconds() - this.timeFrameInSeconds);

      if (this.timeFrameInSeconds >= 259200) {
        to.setHours(23, 59, 59, 999);
        from.setHours(0, 0, 0, 0);
      }
      this.selectedDates = [from, to];
      this.sharedService.change(this.selectedDates);

      if (this.comparativeSelectionActive) {
        let comparativeTo = new Date (from);
        let comparativeFrom = new Date(from);

        comparativeTo.setSeconds(comparativeTo.getSeconds() - 1);
        comparativeFrom.setSeconds(comparativeTo.getSeconds() - this.timeFrameInSeconds);
        if (this.timeFrameInSeconds >= 259200) {
          comparativeFrom.setHours(0, 0, 0, 0);
        }
        this.selectedComparativeDates = [comparativeFrom, comparativeTo];
      }
    }
    else if (this.comparativeSelectionActive) {
      let from = this.selectedDates[0];
      let to = this.selectedDates[1];

      let comparativeTo = new Date(from);
      let comparativeFrom = new Date(from);
      comparativeTo.setSeconds(comparativeTo.getSeconds() - 1);

      let timeZoneOffsetInMilliSeconds = (to.getTimezoneOffset() - from.getTimezoneOffset()) * 60000;
      let timeFrameInMilliseconds = (to.getTime() - from.getTime() - timeZoneOffsetInMilliSeconds); 
      comparativeFrom.setMilliseconds(comparativeTo.getMilliseconds() - timeFrameInMilliseconds);
      this.selectedComparativeDates = [comparativeFrom, comparativeTo];
    }
  }

  updateFromDate(calendar: CalendarType): void {
    this.calendarEventSubscription.unsubscribe();

    if (calendar === CalendarType.ComparativeFrom) {
      if (this.selectedComparativeDates[0] !== this.comparativeDateTimeFrom.selecteds[0]) {
        this.timeFrameInSeconds = this.selectableTimeFramesInSeconds[0];
      }
      this.selectedComparativeDates = this.comparativeDateTimeFrom.selecteds;
    } else {
      if (this.selectedDates[0] !== this.dateTimeFrom.selecteds[0]) {
        this.timeFrameInSeconds = this.selectableTimeFramesInSeconds[0];
      }
      this.selectedDates = this.dateTimeFrom.selecteds;
    }
    this.sharedService.change(this.selectedDates);
  }

  updateToDate(calendar: CalendarType): void {
    this.calendarEventSubscription.unsubscribe();

    if (calendar === CalendarType.ComparativeTo) {
      if (this.selectedComparativeDates[1] !== this.comparativeDateTimeTo.selecteds[1]) {
        this.timeFrameInSeconds = this.selectableTimeFramesInSeconds[0];
      }
      this.selectedComparativeDates = this.comparativeDateTimeTo.selecteds;
    } else {
      if (this.selectedDates[1] !== this.dateTimeTo.selecteds[1]) {
        this.timeFrameInSeconds = this.selectableTimeFramesInSeconds[0];
      }
      this.selectedDates = this.dateTimeTo.selecteds;
    }
      this.sharedService.change(this.selectedDates);
  }

  observeCalendarClicks(calendar: CalendarType): void {
    const calendarDates: HTMLElement = document.querySelector('owl-date-time-calendar');

    this.calendarClick$ = fromEvent<MouseEvent>(calendarDates, 'click');
    this.calendarEnter$ = fromEvent<KeyboardEvent>(calendarDates, 'keydown').pipe(
      filter(event => event.key === 'Enter')
    );

    this.calendarEventSubscription = merge(this.calendarClick$, this.calendarEnter$).subscribe((event) => {
      if ((event.target as HTMLTableDataCellElement).matches('owl-date-time-month-view > table > tbody > tr > td')
        || (event.target as HTMLSpanElement).matches('owl-date-time-month-view > table > tbody > tr > td > span')) {
        if (calendar === CalendarType.From) {
          this.dateTimeFrom.close();
          this.dateTimeTo.open();
        } else if (calendar === CalendarType.To) {
          this.dateTimeTo.close();
        } else if (calendar === CalendarType.ComparativeFrom) {
          this.comparativeDateTimeFrom.close();
          this.comparativeDateTimeTo.open();
        } else {
          this.comparativeDateTimeTo.close();
        }
      }
    });
  }

  toggleComparativeSelection() {
    if (!this.comparativeSelectionActive) {
      this.comparativeSelectionActive = !this.comparativeSelectionActive;
      this.selectTimeFrame();
      return
    }
    this.comparativeSelectionActive = !this.comparativeSelectionActive;
  }
}

export enum CalendarType {
  From = 0,
  To = 1,
  ComparativeFrom = 2,
  ComparativeTo = 3
}
