import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {TimeFrameComponent} from './time-frame.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {OsmLangService} from "../../../../services/osm-lang.service";
import {GrailsBridgeService} from "../../../../services/grails-bridge.service";
import {By} from "@angular/platform-browser";
import {ResultSelectionStore} from "../../services/result-selection.store";
import {ResultSelectionService} from "../../services/result-selection.service";
import {TIME_FRAME_IN_SECONDS} from 'src/app/modules/result-selection/components/time-frame/time-frame-in-seconds.enum';

describe('TimeFrameComponent', () => {
  let component: TimeFrameComponent;
  let fixture: ComponentFixture<TimeFrameComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TimeFrameComponent],
      imports: [SharedMocksModule],
      providers: [
        ResultSelectionStore,
        OsmLangService,
        GrailsBridgeService,
        ResultSelectionService
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TimeFrameComponent);
    component = fixture.componentInstance;
    component.showComparativeTimeFrame = true;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('The dates according to the time frame selection', () => {
    let timeInMilliseconds: number[];

    beforeEach(function() {
      timeInMilliseconds = component.selectableTimeFramesInSeconds.map(number => number * 1000);
    });

    it('should be 3 days for the initial selection', function() {
      expect(component.timeFrameInSeconds).toEqual(TIME_FRAME_IN_SECONDS.THREE_DAYS);
      expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[4]);
    });

    it('should correctly be represented for the 1 hour selection', function() {
      component.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.ONE_HOUR;
      component.selectTimeFrame();
      expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[1]);
    });

    it('should correctly be represented for the 12 hours selection', function() {
      component.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.TWELVE_HOURS;
      component.selectTimeFrame();
      expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[2]);
    });

    it('should correctly be represented for the 1 day selection', function() {
      component.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.ONE_DAY;
      component.selectTimeFrame();
      expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[3]);
    });

    it('should correctly be represented for the 1 week selection', function() {
      component.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.ONE_WEEK;
      component.selectTimeFrame();
      expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[5]);
    });

    it('should correctly be represented for the 2 weeks selection', function() {
      component.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.TWO_WEEKS;
      component.selectTimeFrame();
      expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[6]);
    });

    it('should correctly be represented for the 4 weeks selection', function() {
      component.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.FOUR_WEEKS;
      component.selectTimeFrame();
      expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[7]);
    });
  });

  describe('The comparative dates according to the time frame selection', () => {
    let timeInMilliseconds: number[];
    
    beforeEach(function() {
      timeInMilliseconds = component.selectableTimeFramesInSeconds.map(number => number * 1000);
      const showComparativeSelection: HTMLButtonElement = fixture.nativeElement.querySelector('#show-comparative-button');
      showComparativeSelection.click();
      fixture.detectChanges();
    });

    it('should be set correctly for initial load', function() {
      expect(component.timeFrameInSeconds).toEqual(TIME_FRAME_IN_SECONDS.THREE_DAYS);
      expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[4]);
      expect(component.selectedComparativeDates[1] === component.selectedDates[0]);
    });

    it('should correctly be represented for the 1 hour selection', function() {
      component.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.ONE_HOUR;
      component.selectTimeFrame();
      expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[1]);
      expect(component.selectedComparativeDates[1] === component.selectedDates[0]);
    });

    it('should correctly be represented for the 12 hours selection', function() {
      component.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.TWELVE_HOURS;
      component.selectTimeFrame();
      expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[2]);
      expect(component.selectedComparativeDates[1] === component.selectedDates[0]);
    });

    it('should correctly be represented for the 1 day selection', function() {
      component.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.ONE_DAY;
      component.selectTimeFrame();
      expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[3]);
      expect(component.selectedComparativeDates[1] === component.selectedDates[0]);
    });

    it('should correctly be represented for the 1 week selection', function() {
      component.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.ONE_WEEK;
      component.selectTimeFrame();
      expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[5]);
      expect(component.selectedComparativeDates[1] === component.selectedDates[0]);
    });

    it('should correctly be represented for the 2 weeks selection', function() {
      component.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.TWO_WEEKS;
      component.selectTimeFrame();
      expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[6]);
      expect(component.selectedComparativeDates[1] === component.selectedDates[0]);
    });

    it('should correctly be represented for the 4 weeks selection', function() {
      component.timeFrameInSeconds = TIME_FRAME_IN_SECONDS.FOUR_WEEKS;
      component.selectTimeFrame();
      expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[7]);
      expect(component.selectedComparativeDates[1] === component.selectedDates[0]);
    });
  });

  it('should have a comparative time frame if option is selected', () => {
    expect(fixture.debugElement.query(By.css('#comparative-date-time-picker'))).toBeFalsy();
    const showComparativeSelection: HTMLButtonElement = fixture.nativeElement.querySelector('#show-comparative-button');
    showComparativeSelection.click();
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#comparative-date-time-picker'))).toBeTruthy();
    const hideComparativeSelection: HTMLLinkElement = fixture.nativeElement.querySelector('a');
    hideComparativeSelection.click();
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#comparative-date-time-picker'))).toBeFalsy();
  });
});

function getTimeFrameInMilliseconds(dates: Date[]): number {
  return (dates[1].getTime() - dates[0].getTime()) - ((dates[1].getTimezoneOffset() - dates[0].getTimezoneOffset()) * 60000);
}
