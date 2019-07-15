import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PageComponent} from './page.component';
import {ApplicationService} from '../../../../services/application.service';
import {PageMetricComponent} from "../page-metric/page-metric.component";
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {CsiValueMediumComponent} from "../../../shared/components/csi-value/csi-value-medium/csi-value-medium.component";
import {CsiValueBaseComponent} from "../../../shared/components/csi-value/csi-value-base.component";
import {MeasurandSelectComponent} from "../../../result-selection/components/measurands/measurand-select/measurand-select.component";
import {ResultSelectionModule} from "../../../result-selection/result-selection.module";
import {ResultSelectionService} from "../../../result-selection/services/result-selection.service";
import {Application} from "../../../../models/application.model";

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
        MeasurandSelectComponent
      ],
      imports: [
        SharedMocksModule
      ],
      providers: [
        ApplicationService,
        ResultSelectionModule,
        ResultSelectionService
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PageComponent);
    component = fixture.componentInstance;
    component.application = new Application({name: 'app', id: 1});
    component.aspectTypes = [{name: 'aspect-type-name', icon: 'aspect-type-icon', unit: 'aspect-type-unit'}];
    component.metricsForPage = {
      pageId: 1,
      pageName: 'page',
      PAGE_CONSTRUCTION_STARTED: 2000,
      PAGE_SHOWS_USEFUL_CONTENT: 1800,
      PAGE_IS_USABLE: 3000
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
