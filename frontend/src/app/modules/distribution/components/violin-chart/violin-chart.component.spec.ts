import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ViolinChartComponent} from './violin-chart.component';
import {SharedModule} from '../../../shared/shared.module';
import {SharedMocksModule} from '../../../../testing/shared-mocks.module';

describe('ViolinChartComponent', () => {
  let component: ViolinChartComponent;
  let fixture: ComponentFixture<ViolinChartComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ViolinChartComponent],
      imports: [SharedModule, SharedMocksModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViolinChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
