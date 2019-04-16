import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultSelectionJobGroupComponent } from './result-selection-job-group.component';

describe('ResultSelectionTimeFrameComponent', () => {
  let component: ResultSelectionJobGroupComponent;
  let fixture: ComponentFixture<ResultSelectionJobGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResultSelectionJobGroupComponent ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultSelectionJobGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
