import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MeasurandsComponent } from './measurands.component';
import {MeasurandSelectComponent} from "../measurand-select/measurand-select.component";
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {ResultSelectionStore} from "../../services/result-selection.store";

describe('MeasurandsComponent', () => {
  let component: MeasurandsComponent;
  let fixture: ComponentFixture<MeasurandsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ SharedMocksModule ],
      declarations: [ MeasurandsComponent, MeasurandSelectComponent ],
      providers: [ ResultSelectionStore ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MeasurandsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
