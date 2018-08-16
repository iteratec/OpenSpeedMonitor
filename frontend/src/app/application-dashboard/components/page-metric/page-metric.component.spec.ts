import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PageMetricComponent} from './page-metric.component';
import {SharedMocksModule} from "../../../testing/shared-mocks.module";
import {ApplicationDashboardService} from "../../services/application-dashboard.service";

describe('PageMetricComponent', () => {
  let component: PageMetricComponent;
  let fixture: ComponentFixture<PageMetricComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        PageMetricComponent
      ],
      imports: [
        SharedMocksModule
      ],
      providers: [
        ApplicationDashboardService
      ]
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
