import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ResetComponent} from './reset.component';
import {ResultSelectionStore} from '../../services/result-selection.store';
import {ResultSelectionService} from '../../services/result-selection.service';
import {SharedMocksModule} from '../../../../testing/shared-mocks.module';

describe('ResetComponent', () => {
  let component: ResetComponent;
  let fixture: ComponentFixture<ResetComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ResetComponent],
      imports: [SharedMocksModule],
      providers: [
        ResultSelectionStore,
        ResultSelectionService
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
