import {async, ComponentFixture, TestBed} from "@angular/core/testing";

import {MeasuredEventComponent} from "./measured-event.component";
import {ThresholdComponent} from "../threshold/threshold.component";
import {MeasurandService} from "../../services/measurand.service";
import {ThresholdRowComponent} from "../threshold-row/threshold-row.component";
import {SharedMocksModule} from "../../../testing/shared-mocks.module";
import {SharedModule} from "../../../shared/shared.module";

describe('MeasuredEventComponent', () => {
  let component: MeasuredEventComponent;
  let fixture: ComponentFixture<MeasuredEventComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [SharedModule, SharedMocksModule],
      declarations: [MeasuredEventComponent, ThresholdComponent, ThresholdRowComponent],
      providers: [MeasurandService]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MeasuredEventComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
