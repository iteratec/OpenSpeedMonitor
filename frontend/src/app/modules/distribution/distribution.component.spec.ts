import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DistributionComponent } from './distribution.component';
import {SharedMocksModule} from "../../testing/shared-mocks.module";
import {ResultSelectionModule} from "../result-selection/result-selection.module";
import {OsmLangService} from "../../services/osm-lang.service";
import {GrailsBridgeService} from "../../services/grails-bridge.service";

describe('DistributionComponent', () => {
  let component: DistributionComponent;
  let fixture: ComponentFixture<DistributionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DistributionComponent ],
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
    fixture = TestBed.createComponent(DistributionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
