import {async, ComponentFixture, TestBed} from "@angular/core/testing";

import {ThresholdRowComponent} from "./threshold-row.component";
import {SharedMocksModule} from "../../../testing/shared-mocks.module";
import {SharedModule} from "../../../shared/shared.module";

describe('ThresholdRowComponent', () => {
  let component: ThresholdRowComponent;
  let fixture: ComponentFixture<ThresholdRowComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [SharedModule, SharedMocksModule],
      declarations: [ThresholdRowComponent],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ThresholdRowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
