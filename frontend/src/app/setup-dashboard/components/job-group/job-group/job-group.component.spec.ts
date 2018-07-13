import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JobGroupComponent } from './job-group.component';

describe('JobGroupComponent', () => {
  let component: JobGroupComponent;
  let fixture: ComponentFixture<JobGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JobGroupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JobGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
