import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PerformanceAspectInspectComponent } from './performance-aspect-inspect.component';
import {SharedMocksModule} from "../../../../../testing/shared-mocks.module";
import {MeasurandSelectComponent} from "../../../../result-selection/components/measurand-select/measurand-select.component";
import {MeasurandGroupComponent} from "../../../../result-selection/components/measurand-select/measurand-group/measurand-group.component";
import {ApplicationService} from "../../../../../services/application.service";
import {ResultSelectionService} from "../../../../../services/result-selection.service";

describe('PerformanceAspectInspectComponent', () => {
  let component: PerformanceAspectInspectComponent;
  let fixture: ComponentFixture<PerformanceAspectInspectComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ SharedMocksModule ],
      declarations: [ PerformanceAspectInspectComponent, MeasurandSelectComponent, MeasurandGroupComponent ],
      providers: [ApplicationService, ResultSelectionService]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PerformanceAspectInspectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
