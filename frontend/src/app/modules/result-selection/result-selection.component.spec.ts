import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ResultSelectionComponent} from './result-selection.component';
import {ResultSelectionTimeFrameComponent} from "./components/result-selection-time-frame/result-selection-time-frame.component";
import {SharedMocksModule} from "../../testing/shared-mocks.module";
import {ResultSelectionService} from "./services/result-selection.service";
import {OsmLangService} from "../../services/osm-lang.service";
import {GrailsBridgeService} from "../../services/grails-bridge.service";
import {MeasurandsComponent} from "./components/measurands/measurands.component";

describe('ResultSelectionComponent', () => {
  let component: ResultSelectionComponent;
  let fixture: ComponentFixture<ResultSelectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ResultSelectionComponent,
        ResultSelectionTimeFrameComponent,
        MeasurandsComponent
      ],
      imports: [
        SharedMocksModule
      ],
      providers: [
        ResultSelectionService,
        OsmLangService,
        GrailsBridgeService
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
