import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PerformanceAspectInspectComponent} from './performance-aspect-inspect.component';
import {SharedMocksModule} from "../../../../../testing/shared-mocks.module";
import {MeasurandSelectComponent} from "../../../../result-selection/components/measurand-select/measurand-select.component";
import {ApplicationService} from "../../../../../services/application.service";
import {ResultSelectionService} from "../../../../result-selection/services/result-selection.service";

describe('PerformanceAspectInspectComponent', () => {
  let component: PerformanceAspectInspectComponent;
  let fixture: ComponentFixture<PerformanceAspectInspectComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ SharedMocksModule ],
      declarations: [PerformanceAspectInspectComponent, MeasurandSelectComponent],
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
