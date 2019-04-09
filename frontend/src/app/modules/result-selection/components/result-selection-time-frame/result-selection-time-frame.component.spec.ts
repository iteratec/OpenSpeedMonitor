import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ResultSelectionTimeFrameComponent} from './result-selection-time-frame.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {ResultSelectionService} from "../../services/result-selection.service";
import {OsmLangService} from "../../../../services/osm-lang.service";
import {GrailsBridgeService} from "../../../../services/grails-bridge.service";

describe('ResultSelectionTimeFrameComponent', () => {
  let component: ResultSelectionTimeFrameComponent;
  let fixture: ComponentFixture<ResultSelectionTimeFrameComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ResultSelectionTimeFrameComponent],
      imports: [SharedMocksModule],
      providers: [
        ResultSelectionService,
        OsmLangService,
        GrailsBridgeService]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultSelectionTimeFrameComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
