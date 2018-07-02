import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MeasuredEventComponent } from './measured-event.component';

describe('MeasuredEventComponent', () => {
  let component: MeasuredEventComponent;
  let fixture: ComponentFixture<MeasuredEventComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MeasuredEventComponent ]
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
