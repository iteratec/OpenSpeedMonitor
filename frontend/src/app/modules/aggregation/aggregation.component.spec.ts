import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AggregationComponent } from './aggregation.component';
import {BarchartDataService} from "../chart/services/barchart-data.service";
import {ResultSelectionStore} from "../result-selection/services/result-selection.store";
import {OsmLangService} from "../../services/osm-lang.service";

describe('AggregationComponent', () => {
  let component: AggregationComponent;
  let fixture: ComponentFixture<AggregationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AggregationComponent ],
      providers: [
        BarchartDataService,
        ResultSelectionStore,
        OsmLangService
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AggregationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
