import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ResultSelectionPageLocationConnectivityComponent} from './result-selection-page-location-connectivity.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {ResultSelectionService} from "../../services/result-selection.service";

describe('ResultSelectionPageLocationConnectivityComponent', () => {
  let component: ResultSelectionPageLocationConnectivityComponent;
  let fixture: ComponentFixture<ResultSelectionPageLocationConnectivityComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ResultSelectionPageLocationConnectivityComponent],
      imports: [SharedMocksModule],
      providers: [ResultSelectionService]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultSelectionPageLocationConnectivityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
