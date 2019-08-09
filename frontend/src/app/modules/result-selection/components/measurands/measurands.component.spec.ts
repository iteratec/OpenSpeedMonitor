import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MeasurandsComponent } from './measurands.component';
import {MeasurandSelectComponent} from "./measurand-select/measurand-select.component";
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {ResultSelectionStore} from "../../services/result-selection.store";
import {ResultSelectionService} from "../../services/result-selection.service";
import {By} from "@angular/platform-browser";
import {MeasurandGroup} from "../../../../models/measurand.model";

describe('MeasurandsComponent', () => {
  let component: MeasurandsComponent;
  let fixture: ComponentFixture<MeasurandsComponent>;
  let resultSelectionStore: ResultSelectionStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ SharedMocksModule ],
      declarations: [ MeasurandsComponent, MeasurandSelectComponent ],
      providers: [
        ResultSelectionStore,
        ResultSelectionService
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MeasurandsComponent);
    resultSelectionStore = TestBed.get(ResultSelectionStore);
    component = fixture.componentInstance;
    component.multipleMeasurands = true;

    const loadTimes: MeasurandGroup = {
      isLoading: false,
      name: 'frontend.de.iteratec.isr.measurand.group.LOAD_TIMES',
      values: [
        {
          kind: "selectable-measurand",
          id: 'DOC_COMPLETE_TIME',
          name: 'frontend.de.iteratec.isr.measurand.DOC_COMPLETE_TIME'
        },
        {
          kind: "selectable-measurand",
          id: 'DOM_TIME',
          name: 'frontend.de.iteratec.isr.measurand.DOM_TIME'
        },
        {
          kind: "selectable-measurand",
          id: 'FIRST_BYTE',
          name: 'frontend.de.iteratec.isr.measurand.FIRST_BYTE'
        }
      ]
    };
    resultSelectionStore.loadTimes$.next(loadTimes);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should select multiple measurands if measurands are added', () => {
    expect(component.selectedMeasurands.length).toBe(1);
    expect(fixture.debugElement.queryAll(By.directive(MeasurandSelectComponent)).length).toBe(1);
    const addMeasurand: HTMLButtonElement = fixture.nativeElement.querySelector('#add-measurand-button');
    addMeasurand.click();
    fixture.detectChanges();
    expect(component.selectedMeasurands.length).toBe(2);
    expect(fixture.debugElement.queryAll(By.directive(MeasurandSelectComponent)).length).toBe(2);
    addMeasurand.click();
    fixture.detectChanges();
    expect(component.selectedMeasurands.length).toBe(3);
    expect(fixture.debugElement.queryAll(By.directive(MeasurandSelectComponent)).length).toBe(3);
  });

  it('should deselect measurands if removed until only one measurand is available', () => {
    expect(component.selectedMeasurands.length).toBe(1);
    expect(fixture.debugElement.queryAll(By.directive(MeasurandSelectComponent)).length).toBe(1);
    const addMeasurand: HTMLButtonElement = fixture.nativeElement.querySelector('#add-measurand-button');
    addMeasurand.click();
    fixture.detectChanges();
    expect(component.selectedMeasurands.length).toBe(2);
    expect(fixture.debugElement.queryAll(By.directive(MeasurandSelectComponent)).length).toBe(2);

    const removeMeasurand: HTMLButtonElement = fixture.nativeElement.querySelector('#remove-measurand-button');
    removeMeasurand.click();
    fixture.detectChanges();
    expect(component.selectedMeasurands.length).toBe(1);
    expect(fixture.debugElement.queryAll(By.directive(MeasurandSelectComponent)).length).toBe(1);
    expect(fixture.nativeElement.querySelector('#remove-measurand-button')).toBeFalsy();
  });
});
