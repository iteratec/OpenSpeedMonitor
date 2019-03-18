import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MeasurandGroupComponent } from './measurand-group.component';

describe('MeasurandGroupComponent', () => {
  let component: MeasurandGroupComponent;
  let fixture: ComponentFixture<MeasurandGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MeasurandGroupComponent ]
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
