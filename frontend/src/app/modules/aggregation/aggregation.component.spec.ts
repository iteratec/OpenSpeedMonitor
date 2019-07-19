import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {AggregationComponent} from './aggregation.component';
import {SharedMocksModule} from "../../testing/shared-mocks.module";
import {ResultSelectionModule} from "../result-selection/result-selection.module";
import {BarchartDataService} from "./services/barchart-data.service";
import {OsmLangService} from "../../services/osm-lang.service";
import {GrailsBridgeService} from "../../services/grails-bridge.service";

describe('AggregationComponent', () => {
  let component: AggregationComponent;
  let fixture: ComponentFixture<AggregationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AggregationComponent ],
      imports: [
        SharedMocksModule,
        ResultSelectionModule
      ],
      providers: [
        BarchartDataService,
        OsmLangService,
        GrailsBridgeService
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
