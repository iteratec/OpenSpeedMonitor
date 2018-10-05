import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ApplicationDashboardComponent} from './application-dashboard.component';
import {PageComponent} from './components/page/page.component';
import {CsiValueBaseComponent} from './components/csi-value/csi-value-base.component';
import {ApplicationSelectComponent} from './components/application-select/application-select.component';
import {SharedMocksModule} from '../testing/shared-mocks.module';
import {ApplicationDashboardService} from './services/application-dashboard.service';
import {CsiGraphComponent} from './components/csi-graph/csi-graph.component';
import {PageMetricComponent} from "./components/page-metric/page-metric.component";
import {CsiInfoComponent} from "./components/csi-info/csi-info.component";
import {CsiValueBigComponent} from "./components/csi-value/csi-value-big/csi-value-big.component";
import {CsiValueMediumComponent} from "./components/csi-value/csi-value-medium/csi-value-medium.component";
import {CsiValueSmallComponent} from "./components/csi-value/csi-value-small/csi-value-small.component";

describe('ApplicationDashboardComponent', () => {
  let component: ApplicationDashboardComponent;
  let fixture: ComponentFixture<ApplicationDashboardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ApplicationDashboardComponent,
        PageComponent,
        CsiValueBaseComponent,
        CsiValueBigComponent,
        CsiValueMediumComponent,
        CsiValueSmallComponent,
        CsiGraphComponent,
        CsiInfoComponent,
        ApplicationSelectComponent,
        PageMetricComponent
      ],
      providers: [
        ApplicationDashboardService
      ],
      imports: [
        SharedMocksModule
      ]
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
