import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PerformanceAspectManagementComponent } from './performance-aspect-management.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {PerformanceAspectInspectComponent} from "./performance-aspect-inspect/performance-aspect-inspect.component";
import {MeasurandSelectComponent} from "../../../result-selection/components/measurand-select/measurand-select.component";
import {MeasurandGroupComponent} from "../../../result-selection/components/measurand-select/measurand-group/measurand-group.component";
import {ApplicationService} from "../../../../services/application.service";
import {ResultSelectionService} from "../../../../services/result-selection.service";

describe('PerformanceAspectManagementComponent', () => {
  let component: PerformanceAspectManagementComponent;
  let fixture: ComponentFixture<PerformanceAspectManagementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ SharedMocksModule ],
      declarations: [ PerformanceAspectManagementComponent, PerformanceAspectInspectComponent, MeasurandSelectComponent, MeasurandGroupComponent ],
      providers: [ApplicationService, ResultSelectionService]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PerformanceAspectManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
