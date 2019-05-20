import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AspectMetricsComponent } from './aspect-metrics.component';

describe('AspectMetricsComponent', () => {
  let component: AspectMetricsComponent;
  let fixture: ComponentFixture<AspectMetricsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AspectMetricsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AspectMetricsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
