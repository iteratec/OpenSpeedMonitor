import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PageComponent} from './page.component';
import {ApplicationService} from '../../../../services/application.service';
import {PageMetricComponent} from "../page-metric/page-metric.component";
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {CsiValueMediumComponent} from "../../../shared/components/csi-value/csi-value-medium/csi-value-medium.component";
import {CsiValueBaseComponent} from "../../../shared/components/csi-value/csi-value-base.component";
import {PerformanceAspectManagementComponent} from "../performance-aspect-management/performance-aspect-management.component";
import {PerformanceAspectInspectComponent} from "../performance-aspect-management/performance-aspect-inspect/performance-aspect-inspect.component";
import {MeasurandSelectComponent} from "../../../result-selection/components/measurand-select/measurand-select.component";
import {MeasurandGroupComponent} from "../../../result-selection/components/measurand-select/measurand-group/measurand-group.component";
import {ResultSelectionModule} from "../../../result-selection/result-selection.module";

describe('PageComponent', () => {

  let component: PageComponent;
  let fixture: ComponentFixture<PageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        PageComponent,
        CsiValueMediumComponent,
        CsiValueBaseComponent,
        PageMetricComponent,
        PerformanceAspectManagementComponent,
        PerformanceAspectInspectComponent,
        MeasurandSelectComponent,
        MeasurandGroupComponent
      ],
      imports: [
        SharedMocksModule
      ],
      providers: [
        ApplicationService,
        ResultSelectionModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
