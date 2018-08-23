import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ThresholdGroupComponent} from './threshold-group.component';
import {ThresholdComponent} from '../threshold/threshold.component';
import {MeasurandService} from '../../services/measurand.service';
import {ThresholdRowComponent} from '../threshold-row/threshold-row.component';
import {SharedMocksModule} from '../../../testing/shared-mocks.module';
import {SharedModule} from '../../../shared/shared.module';
import {ThresholdRestService} from '../../services/threshold-rest.service';

describe('ThresholdGroupComponent', () => {
  let component: ThresholdGroupComponent;
  let fixture: ComponentFixture<ThresholdGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [SharedModule, SharedMocksModule],
      declarations: [ThresholdGroupComponent, ThresholdComponent, ThresholdRowComponent],
      providers: [MeasurandService, ThresholdRestService]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ThresholdGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
