import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ApplicationDashboardComponent} from './application-dashboard.component';
import {PageComponent} from './components/page/page.component';
import {CsiValueComponent} from './components/csi-value/csi-value.component';
import {ApplicationSelectComponent} from './components/application-select/application-select.component';
import {ApplicationDashboardService} from './services/application-dashboard.service';
import {CsiGraphComponent} from './components/csi-graph/csi-graph.component';
import {PageMetricComponent} from "./components/page-metric/page-metric.component";
import {CsiInfoComponent} from "./components/csi-info/csi-info.component";
import {SharedMocksModule} from "../../testing/shared-mocks.module";

describe('ApplicationDashboardComponent', () => {
  let component: ApplicationDashboardComponent;
  let fixture: ComponentFixture<ApplicationDashboardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        SharedMocksModule
      ],
      declarations: [
        ApplicationDashboardComponent,
        PageComponent,
        CsiValueComponent,
        CsiGraphComponent,
        CsiInfoComponent,
        ApplicationSelectComponent,
        PageMetricComponent
      ],
      providers: [
        ApplicationDashboardService
      ],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
