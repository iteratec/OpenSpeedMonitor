import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationDashboardEntryComponent } from './application-dashboard-entry.component';

describe('ApplicationDashboardEntryComponent', () => {
  let component: ApplicationDashboardEntryComponent;
  let fixture: ComponentFixture<ApplicationDashboardEntryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationDashboardEntryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationDashboardEntryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
