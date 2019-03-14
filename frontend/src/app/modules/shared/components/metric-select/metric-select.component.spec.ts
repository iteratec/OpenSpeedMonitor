import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MetricSelectComponent } from './metric-select.component';

describe('MetricSelectComponent', () => {
  let component: MetricSelectComponent;
  let fixture: ComponentFixture<MetricSelectComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MetricSelectComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricSelectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
