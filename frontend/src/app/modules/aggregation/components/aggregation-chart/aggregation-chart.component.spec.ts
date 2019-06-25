import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AggregationChartComponent } from './aggregation-chart.component';

describe('AggregationChartComponent', () => {
  let component: AggregationChartComponent;
  let fixture: ComponentFixture<AggregationChartComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AggregationChartComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AggregationChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
