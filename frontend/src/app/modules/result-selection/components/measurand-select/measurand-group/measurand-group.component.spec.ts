import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MeasurandGroupComponent } from './measurand-group.component';
import {SharedMocksModule} from "../../../../../testing/shared-mocks.module";

describe('MeasurandGroupComponent', () => {
  let component: MeasurandGroupComponent;
  let fixture: ComponentFixture<MeasurandGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MeasurandGroupComponent ],
      imports: [SharedMocksModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MeasurandGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
