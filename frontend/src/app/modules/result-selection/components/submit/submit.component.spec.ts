import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import {SubmitComponent} from './submit.component';
import {BarchartDataService} from "../../../aggregation/services/barchart-data.service";
import {ResultSelectionStore} from "../../services/result-selection.store";
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {ResultSelectionService} from "../../services/result-selection.service";
import {By} from "@angular/platform-browser";

describe('SubmitComponent', () => {
  let component: SubmitComponent;
  let fixture: ComponentFixture<SubmitComponent>;
  let resultSelectionStore: ResultSelectionStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SubmitComponent ],
      imports: [SharedMocksModule],
      providers: [
        BarchartDataService,
        ResultSelectionStore,
        ResultSelectionService
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SubmitComponent);
    resultSelectionStore = TestBed.get(ResultSelectionStore);
    component = fixture.componentInstance;
    component.applicationsRequired = true;
    component.pagesRequired = true;
    component.measurandsRequired = true;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should enable submit button if application, page and measurand are selected', () => {
    resultSelectionStore.resultCount$.next(100);
    resultSelectionStore._resultSelectionCommand$.next({
      from: new Date(),
      to: new Date(),
      jobGroupIds: [1, 2, 3],
      pageIds: [4, 7, 9]
    });
    resultSelectionStore._remainingResultSelection$.next({
      measurands: ["DOC_COMPLETE_TIME"]
    });
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#bet-barchart-data-submit')).nativeElement.disabled).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#unavailable-data'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-application-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-page-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-measurand-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#long-processing-time'))).toBeFalsy();
  });

  it('should enable submit button if nothing is required', () => {
    component.applicationsRequired = false;
    component.pagesRequired = false;
    component.measurandsRequired = false;
    resultSelectionStore.resultCount$.next(100);
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#bet-barchart-data-submit')).nativeElement.disabled).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#unavailable-data'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-application-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-page-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-measurand-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#long-processing-time'))).toBeFalsy();
  });

  it('should warn of unavailable barchart data', () => {
    expect(fixture.debugElement.query(By.css('#bet-barchart-data-submit')).nativeElement.disabled).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#unavailable-data'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#no-application-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-page-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-measurand-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#long-processing-time'))).toBeFalsy();
  });

  it('should warn of unavailable barchart data, even if application, page and measurand are selected', () => {
    resultSelectionStore._resultSelectionCommand$.next({
      from: new Date(),
      to: new Date(),
      jobGroupIds: [1, 2, 3],
      pageIds: [4, 7, 9]
    });
    resultSelectionStore._remainingResultSelection$.next({
      measurands: ["DOC_COMPLETE_TIME"]
    });
    expect(fixture.debugElement.query(By.css('#bet-barchart-data-submit')).nativeElement.disabled).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#unavailable-data'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#no-application-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-page-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-measurand-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#long-processing-time'))).toBeFalsy();
  });

  it('should warn that no application, page and measurand is selected', () => {
    resultSelectionStore.resultCount$.next(100);
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#bet-barchart-data-submit')).nativeElement.disabled).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#unavailable-data'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-application-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#no-page-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#no-measurand-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#long-processing-time'))).toBeFalsy();
  });

  it('should warn that no application is selected', () => {
    component.pagesRequired = false;
    component.measurandsRequired = false;
    resultSelectionStore.resultCount$.next(100);
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#bet-barchart-data-submit')).nativeElement.disabled).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#unavailable-data'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-application-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#no-page-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-measurand-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#long-processing-time'))).toBeFalsy();
  });

  it('should warn that no page is selected', () => {
    component.applicationsRequired = false;
    component.measurandsRequired = false;
    resultSelectionStore.resultCount$.next(100);
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#bet-barchart-data-submit')).nativeElement.disabled).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#unavailable-data'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-application-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-page-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#no-measurand-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#long-processing-time'))).toBeFalsy();
  });

  it('should warn that no measurand is selected.', () => {
    component.applicationsRequired = false;
    component.pagesRequired = false;
    resultSelectionStore.resultCount$.next(100);
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#bet-barchart-data-submit')).nativeElement.disabled).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#unavailable-data'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-application-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-page-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-measurand-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#long-processing-time'))).toBeFalsy();
  });

  it('should not warn about the selected application', () => {
    resultSelectionStore.resultCount$.next(100);
    component.applicationsSelected$.next(true);
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#bet-barchart-data-submit')).nativeElement.disabled).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#unavailable-data'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-application-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-page-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#no-measurand-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#long-processing-time'))).toBeFalsy();
  });

  it('should not warn about the selected application', () => {
    resultSelectionStore.resultCount$.next(100);
    resultSelectionStore._resultSelectionCommand$.next({
      from: new Date(),
      to: new Date(),
      jobGroupIds: [1, 2, 3]
    });
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#bet-barchart-data-submit')).nativeElement.disabled).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#unavailable-data'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-application-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-page-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#no-measurand-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#long-processing-time'))).toBeFalsy();
  });

  it('should not warn about the selected page', () => {
    resultSelectionStore.resultCount$.next(100);
    resultSelectionStore._resultSelectionCommand$.next({
      from: new Date(),
      to: new Date(),
      pageIds: [3, 5, 6]
    });
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#bet-barchart-data-submit')).nativeElement.disabled).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#unavailable-data'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-application-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#no-page-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-measurand-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#long-processing-time'))).toBeFalsy();
  });

  it('should not warn about the selected meausrand', () => {
    resultSelectionStore.resultCount$.next(100);
    resultSelectionStore._remainingResultSelection$.next({
      measurands: ["DOC_COMPLETE_TIME"]
    });
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#bet-barchart-data-submit')).nativeElement.disabled).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#unavailable-data'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#no-application-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#no-page-selected'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('#no-measurand-selected'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#long-processing-time'))).toBeFalsy();
  });
});
