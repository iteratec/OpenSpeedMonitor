import {Component, Input, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {DateTimeAdapter, OwlDateTimeComponent} from 'ng-pick-datetime';
import {fromEvent, merge, Observable, Subscription} from "rxjs";
import {filter} from "rxjs/operators";
import {OsmLangService} from "../../../../services/osm-lang.service";
import {ResultSelectionStore} from "../../services/result-selection.store";
import {TIME_FRAME_IN_SECONDS} from 'src/app/modules/result-selection/components/time-frame/time-frame-in-seconds.enum';

@Component({
  selector: 'osm-result-selection-time-frame',
  templateUrl: './time-frame.component.html',
  styleUrls: ['./time-frame.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class TimeFrameComponent implements OnInit {

  @Input() showAggregation: boolean = false;
  @Input() showComparativeTimeFrame: boolean = false;
  @ViewChild('dateTimeFrom') dateTimeFrom: OwlDateTimeComponent<Date>;
  @ViewChild('dateTimeTo') dateTimeTo: OwlDateTimeComponent<Date>;
  @ViewChild('comparativeDateTimeFrom') comparativeDateTimeFrom: OwlDateTimeComponent<Date>;
  @ViewChild('comparativeDateTimeTo') comparativeDateTimeTo: OwlDateTimeComponent<Date>;

  calendarClick$: Observable<MouseEvent>;
  calendarEnter$: Observable<KeyboardEvent>;
  calendarEventSubscription: Subscription;

  selectableTimeFramesInSeconds: number[] = [
    TIME_FRAME_IN_SECONDS.MANUAL_SELECTION,
    TIME_FRAME_IN_SECONDS.ONE_HOUR,
    TIME_FRAME_IN_SECONDS.TWELVE_HOURS,
    TIME_FRAME_IN_SECONDS.ONE_DAY,
    TIME_FRAME_IN_SECONDS.THREE_DAYS,
    TIME_FRAME_IN_SECONDS.ONE_WEEK,
    TIME_FRAME_IN_SECONDS.TWO_WEEKS,
    TIME_FRAME_IN_SECONDS.FOUR_WEEKS
  ];
  timeFrameInSeconds: number = TIME_FRAME_IN_SECONDS.MANUAL_SELECTION;

  selectableAggregationIntervalsInSeconds: number[] = [-1, 60, 1440, 10080];
  aggregationIntervalInSeconds: number = this.selectableAggregationIntervalsInSeconds[0];

  selectedDates: Date[];
  selectedComparativeDates: Date[];
  max = new Date();

  comparativeSelectionActive: boolean = false;

  CalendarType: typeof CalendarType = CalendarType;

  constructor(private resultSelectionStore: ResultSelectionStore, dateTimeAdapter: DateTimeAdapter<any>, osmLangService: OsmLangService) {
    if (osmLangService.getOsmLang() == 'en') {
      dateTimeAdapter.setLocale('en-GB');
    } else {
      dateTimeAdapter.setLocale(osmLangService.getOsmLang());
    }
  }

  ngOnInit() {
    this.selectedDates = [this.resultSelectionStore.resultSelectionCommand.from, this.resultSelectionStore.resultSelectionCommand.to];
    this.selectTimeFrame();

    if (this.showAggregation) {
      this.resultSelectionStore.setRemainingResultSelectionInterval(this.aggregationIntervalInSeconds);
    }
  }

  selectTimeFrame(): void {
    if (this.timeFrameInSeconds != TIME_FRAME_IN_SECONDS.MANUAL_SELECTION) {
      this.setDatesFromTimeFrame();

    } else if (this.selectedDates != undefined && this.selectedDates.length == 2) {
      this.setTimeFrameFromDates();

    }

    if (this.comparativeSelectionActive) {
      this.setComparativeDatesFromDateSelection(this.selectedDates[0], this.selectedDates[1]);
    }

    this.resultSelectionStore.setResultSelectionCommandTimeFrame(this.selectedDates);
    if (this.comparativeSelectionActive) {
      this.resultSelectionStore.setRemainingResultSelectionComparativeTimeFrame(this.selectedComparativeDates);
    }
  }

  private setDatesFromTimeFrame(): void {
    let to = new Date();
    let from = new Date(to.getTime() - this.timeFrameInSeconds * 1000);

    this.selectedDates = [from, to];

    if (this.comparativeSelectionActive) {
      this.setComparativeDatesFromDateSelection(from, to);
    }
  }

  private setComparativeDatesFromDateSelection(from: Date, to: Date): void {
    let comparativeFrom = new Date(from);
    let comparativeTo = new Date(from);

    comparativeTo.setSeconds(comparativeTo.getSeconds() - 1);

    let calculatedTimeFrameInSecondes = this.calculateTimeFrameInSeconds(from, to);
    comparativeFrom.setSeconds(comparativeTo.getSeconds() - calculatedTimeFrameInSecondes);

    this.selectedComparativeDates = [comparativeFrom, comparativeTo];
  }

  private setTimeFrameFromDates(): void {
    let from = new Date(this.selectedDates[0]);
    let to = new Date(this.selectedDates[1]);

    // Remove seconds and millisecond
    from.setSeconds(0,0);
    to.setSeconds(0,0);

    let calculatedTimeFrameInSecondes = this.calculateTimeFrameInSeconds(from, to);
    if (this.isValidTimeFrameUntilNow(calculatedTimeFrameInSecondes)) {
      this.timeFrameInSeconds = calculatedTimeFrameInSecondes;
    } else {
      this.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.MANUAL_SELECTION;
    }
  }

  private calculateTimeFrameInSeconds(from: Date, to: Date): number {
    let timeZoneOffsetInSeconds = (to.getTimezoneOffset() - from.getTimezoneOffset()) * 60;
    return to.getTime()/1000 - from.getTime()/1000 - timeZoneOffsetInSeconds;
  }

  private isValidTimeFrameUntilNow(calculatedTimeFrameInSecondes: number): boolean {
    let to = new Date(this.selectedDates[1]);
    to.setHours(23, 59, 0, 0);
    return to >= this.max && this.selectableTimeFramesInSeconds.find(value => value == calculatedTimeFrameInSecondes) != undefined
  }

  updateFromDate(calendar: CalendarType): void {
    this.calendarEventSubscription.unsubscribe();

    if (calendar === CalendarType.ComparativeFrom) {
      if (this.selectedComparativeDates[0] !== this.comparativeDateTimeFrom.selecteds[0]) {
        this.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.MANUAL_SELECTION;
      }
      this.selectedComparativeDates = this.comparativeDateTimeFrom.selecteds;

    } else {
      if (this.selectedDates[0] !== this.dateTimeFrom.selecteds[0]) {
        this.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.MANUAL_SELECTION;
      }
      this.selectedDates = this.dateTimeFrom.selecteds;
    }

    this.resultSelectionStore.setResultSelectionCommandTimeFrame(this.selectedDates);
    if (this.comparativeSelectionActive) {
      this.resultSelectionStore.setRemainingResultSelectionComparativeTimeFrame(this.selectedComparativeDates);
    }
  }

  updateToDate(calendar: CalendarType): void {
    this.calendarEventSubscription.unsubscribe();

    if (calendar === CalendarType.ComparativeTo) {
      if (this.selectedComparativeDates[1] !== this.comparativeDateTimeTo.selecteds[1]) {
        this.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.MANUAL_SELECTION;
      }
      this.selectedComparativeDates = this.comparativeDateTimeTo.selecteds;

    } else {
      if (this.selectedDates[1] !== this.dateTimeTo.selecteds[1]) {
        this.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.MANUAL_SELECTION;
      }
      this.selectedDates = this.dateTimeTo.selecteds;
    }

    this.resultSelectionStore.setResultSelectionCommandTimeFrame(this.selectedDates);
    if (this.comparativeSelectionActive) {
      this.resultSelectionStore.setRemainingResultSelectionComparativeTimeFrame(this.selectedComparativeDates);
    }
  }

  updateMaxDate(): void {
    this.max = new Date();
  }

  observeCalendarClicks(calendar: CalendarType): void {
    this.updateMaxDate();

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
    } else {
      this.comparativeSelectionActive = !this.comparativeSelectionActive;
      this.resultSelectionStore.setRemainingResultSelectionComparativeTimeFrame([null, null]);
    }
  }

  selectInterval() {
    this.resultSelectionStore.setRemainingResultSelectionInterval(this.aggregationIntervalInSeconds);
  }
}

export enum CalendarType {
  From = 0,
  To = 1,
  ComparativeFrom = 2,
  ComparativeTo = 3
}
