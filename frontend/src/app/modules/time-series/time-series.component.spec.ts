import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TimeSeriesComponent } from './time-series.component';
import {SharedMocksModule} from "../../testing/shared-mocks.module";
import {ResultSelectionModule} from "../result-selection/result-selection.module";
import {OsmLangService} from "../../services/osm-lang.service";
import {GrailsBridgeService} from "../../services/grails-bridge.service";

describe('TimeSeriesComponent', () => {
  let component: TimeSeriesComponent;
  let fixture: ComponentFixture<TimeSeriesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TimeSeriesComponent ],
      imports: [
        SharedMocksModule,
        ResultSelectionModule
      ],
      providers: [
        OsmLangService,
        GrailsBridgeService
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TimeSeriesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
