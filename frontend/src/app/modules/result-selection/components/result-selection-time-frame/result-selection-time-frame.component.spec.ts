import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultSelectionTimeFrameComponent } from './result-selection-time-frame.component';

describe('ResultSelectionTimeFrameComponent', () => {
  let component: ResultSelectionTimeFrameComponent;
  let fixture: ComponentFixture<ResultSelectionTimeFrameComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResultSelectionTimeFrameComponent ]
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
});
