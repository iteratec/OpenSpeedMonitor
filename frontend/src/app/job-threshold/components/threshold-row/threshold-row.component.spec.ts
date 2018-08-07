import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ThresholdRowComponent } from './threshold-row.component';

describe('ThresholdRowComponent', () => {
  let component: ThresholdRowComponent;
  let fixture: ComponentFixture<ThresholdRowComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ThresholdRowComponent ]
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
