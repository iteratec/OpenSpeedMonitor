import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PerformanceAspectManagementComponent } from './performance-aspect-management.component';

describe('PerformanceAspectManagementComponent', () => {
  let component: PerformanceAspectManagementComponent;
  let fixture: ComponentFixture<PerformanceAspectManagementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PerformanceAspectManagementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PerformanceAspectManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
