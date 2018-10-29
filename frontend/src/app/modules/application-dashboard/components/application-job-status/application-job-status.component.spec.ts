import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationJobStatusComponent } from './application-job-status.component';

describe('ApplicationJobStatusComponent', () => {
  let component: ApplicationJobStatusComponent;
  let fixture: ComponentFixture<ApplicationJobStatusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationJobStatusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationJobStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
