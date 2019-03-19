import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PerformanceAspectInspectComponent } from './performance-aspect-inspect.component';

describe('PerformanceAspectInspectComponent', () => {
  let component: PerformanceAspectInspectComponent;
  let fixture: ComponentFixture<PerformanceAspectInspectComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PerformanceAspectInspectComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PerformanceAspectInspectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
