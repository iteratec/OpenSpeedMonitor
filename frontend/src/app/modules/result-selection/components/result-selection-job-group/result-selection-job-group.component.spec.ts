import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultSelectionJobGroupComponent } from './result-selection-job-group.component';
import { SelectableApplication } from 'src/app/models/application.model';
import { ResultSelectionService } from '../../services/result-selection.service';
import { SharedMocksModule } from 'src/app/testing/shared-mocks.module';
import { OsmLangService } from 'src/app/services/osm-lang.service';
import { GrailsBridgeService } from 'src/app/services/grails-bridge.service';
import { SharedService } from '../../services/sharedService';

describe('ResultSelectionTimeFrameComponent', () => {
  let component: ResultSelectionJobGroupComponent;
  let fixture: ComponentFixture<ResultSelectionJobGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ResultSelectionJobGroupComponent],
      imports: [SharedMocksModule],
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
    fixture = TestBed.createComponent(ResultSelectionJobGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should correctly represent the job groups according to the filter by tags', () =>{
    const selectableTags: string[] = component.selectableTags;
    const showFilteredApplicationSelection: HTMLButtonElement = fixture.nativeElement.querySelector('#filterBtn');
    showFilteredApplicationSelection.click();
    fixture.detectChanges();

    //expect(component.filteredJobGroups).toEqual();
  });
});
