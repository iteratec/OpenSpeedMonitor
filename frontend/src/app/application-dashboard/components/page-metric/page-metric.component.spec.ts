import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PageMetricComponent } from './page-metric.component';

describe('PageMetricComponent', () => {
  let component: PageMetricComponent;
  let fixture: ComponentFixture<PageMetricComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PageMetricComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PageMetricComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
