import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JobGroupListComponent } from './job-group-list.component';

describe('JobGroupListComponent', () => {
  let component: JobGroupListComponent;
  let fixture: ComponentFixture<JobGroupListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JobGroupListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JobGroupListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
