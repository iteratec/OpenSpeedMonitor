import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ResultSelectionTimeFrameComponent} from './result-selection-time-frame.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {ResultSelectionService} from "../../services/result-selection.service";
import {OsmLangService} from "../../../../services/osm-lang.service";
import {GrailsBridgeService} from "../../../../services/grails-bridge.service";
import {By} from "@angular/platform-browser";

describe('ResultSelectionTimeFrameComponent', () => {
  let component: ResultSelectionTimeFrameComponent;
  let fixture: ComponentFixture<ResultSelectionTimeFrameComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ResultSelectionTimeFrameComponent],
      imports: [SharedMocksModule],
      providers: [
        ResultSelectionService,
        OsmLangService,
        GrailsBridgeService
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultSelectionTimeFrameComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should correctly represent the dates according to the time frame selection', () => {
    const timeInMilliseconds: number[] = component.selectableTimeFramesInSeconds.map(number => number * 1000);

    expect(component.timeFrameInSeconds).toEqual(component.selectableTimeFramesInSeconds[4]);
    expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[4]);

    component.timeFrameInSeconds = component.selectableTimeFramesInSeconds[1];
    component.selectTimeFrame();
    expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[1]);

    component.timeFrameInSeconds = component.selectableTimeFramesInSeconds[2];
    component.selectTimeFrame();
    expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[2]);

    component.timeFrameInSeconds = component.selectableTimeFramesInSeconds[3];
    component.selectTimeFrame();
    expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[3]);

    component.timeFrameInSeconds = component.selectableTimeFramesInSeconds[5];
    component.selectTimeFrame();
    expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[5]);

    component.timeFrameInSeconds = component.selectableTimeFramesInSeconds[6];
    component.selectTimeFrame();
    expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[6]);

    component.timeFrameInSeconds = component.selectableTimeFramesInSeconds[7];
    component.selectTimeFrame();
    expect(getTimeFrameInMilliseconds(component.selectedDates)).toEqual(timeInMilliseconds[7]);
  });

  it('should correctly represent the comparative dates according to the time frame selection', () => {
    const timeInMilliseconds: number[] = component.selectableTimeFramesInSeconds.map(number => number * 1000);
    const showComparativeSelection: HTMLButtonElement = fixture.nativeElement.querySelector('#show-comparative-button');
    showComparativeSelection.click();
    fixture.detectChanges();

    expect(component.timeFrameInSeconds).toEqual(component.selectableTimeFramesInSeconds[4]);
    expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[4]);
    expect(component.selectedComparativeDates[1] === component.selectedDates[0]);

    component.timeFrameInSeconds = component.selectableTimeFramesInSeconds[1];
    component.selectTimeFrame();
    expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[1]);
    expect(component.selectedComparativeDates[1] === component.selectedDates[0]);

    component.timeFrameInSeconds = component.selectableTimeFramesInSeconds[2];
    component.selectTimeFrame();
    expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[2]);
    expect(component.selectedComparativeDates[1] === component.selectedDates[0]);

    component.timeFrameInSeconds = component.selectableTimeFramesInSeconds[3];
    component.selectTimeFrame();
    expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[3]);
    expect(component.selectedComparativeDates[1] === component.selectedDates[0]);

    component.timeFrameInSeconds = component.selectableTimeFramesInSeconds[5];
    component.selectTimeFrame();
    expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[5]);
    expect(component.selectedComparativeDates[1] === component.selectedDates[0]);

    component.timeFrameInSeconds = component.selectableTimeFramesInSeconds[6];
    component.selectTimeFrame();
    expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[6]);
    expect(component.selectedComparativeDates[1] === component.selectedDates[0]);

    component.timeFrameInSeconds = component.selectableTimeFramesInSeconds[7];
    component.selectTimeFrame();
    expect(getTimeFrameInMilliseconds(component.selectedComparativeDates)).toEqual(timeInMilliseconds[7]);
    expect(component.selectedComparativeDates[1] === component.selectedDates[0]);
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
