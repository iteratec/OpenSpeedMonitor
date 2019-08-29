import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {AggregationComponent} from './aggregation.component';
import {SharedMocksModule} from "../../testing/shared-mocks.module";
import {ResultSelectionModule} from "../result-selection/result-selection.module";
import {BarchartDataService} from "./services/barchart-data.service";
import {OsmLangService} from "../../services/osm-lang.service";
import {GrailsBridgeService} from "../../services/grails-bridge.service";
import {AggregationChartDataService} from "./services/aggregation-chart-data.service";
import {AggregationChartComponent} from "./components/aggregation-chart/aggregation-chart.component";
import {SpinnerComponent} from "../shared/components/spinner/spinner.component";


describe('AggregationComponent', () => {
  let component: AggregationComponent;
  let fixture: ComponentFixture<AggregationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AggregationComponent, AggregationChartComponent ],
      imports: [
        SharedMocksModule,
        ResultSelectionModule,
        SpinnerComponent
      ],
      providers: [
        BarchartDataService,
        OsmLangService,
        GrailsBridgeService,
        AggregationChartDataService
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
