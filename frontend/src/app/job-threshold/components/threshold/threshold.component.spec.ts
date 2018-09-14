import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ThresholdComponent} from './threshold.component';
import {ThresholdRowComponent} from '../threshold-row/threshold-row.component';
import {ThresholdRestService} from '../../services/threshold-rest.service';
import {SharedMocksModule} from '../../../testing/shared-mocks.module';
import {SharedModule} from '../../../shared/shared.module';
import {ThresholdService} from '../../services/threshold.service';

describe('ThresholdComponent', () => {
  let component: ThresholdComponent;
  let fixture: ComponentFixture<ThresholdComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [SharedModule, SharedMocksModule],
      declarations: [ThresholdComponent, ThresholdRowComponent],
      providers: [ThresholdRestService, ThresholdService]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ThresholdComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
