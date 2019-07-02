import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import {SubmitComponent} from './submit.component';
import {BarchartDataService} from "../../../chart/services/barchart-data.service";
import {ResultSelectionStore} from "../../services/result-selection.store";
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {ResultSelectionService} from "../../services/result-selection.service";

describe('SubmitComponent', () => {
  let component: SubmitComponent;
  let fixture: ComponentFixture<SubmitComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SubmitComponent ],
      imports: [SharedMocksModule],
      providers: [
        BarchartDataService,
        ResultSelectionStore,
        ResultSelectionService
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SubmitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
