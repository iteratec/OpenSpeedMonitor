import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultSelectionComponent } from './result-selection.component';
import { ResultSelectionTimeFrameComponent } from './components/result-selection-time-frame/result-selection-time-frame.component';
import { ResultSelectionJobGroupComponent } from './components/result-selection-job-group/result-selection-job-group.component';
import { SharedMocksModule } from 'src/app/testing/shared-mocks.module';
import { ResultSelectionService } from './services/result-selection.service';
import { OsmLangService } from 'src/app/services/osm-lang.service';
import { GrailsBridgeService } from 'src/app/services/grails-bridge.service';
import { SharedService } from './services/sharedService';

describe('ResultSelectionComponent', () => {
  let component: ResultSelectionComponent;
  let fixture: ComponentFixture<ResultSelectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ResultSelectionComponent,
        ResultSelectionTimeFrameComponent,
        ResultSelectionJobGroupComponent
      ],
      imports: [
        SharedMocksModule
      ],
      providers: [
        ResultSelectionService,
        OsmLangService,
        GrailsBridgeService,
        SharedService
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
