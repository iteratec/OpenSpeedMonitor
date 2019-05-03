import { async, ComponentFixture, TestBed} from '@angular/core/testing';
import { ResultSelectionJobGroupComponent } from './result-selection-job-group.component';
import { SelectableApplication } from 'src/app/models/application.model';
import { ResultSelectionService } from '../../services/result-selection.service';
import { SharedMocksModule } from 'src/app/testing/shared-mocks.module';
import { OsmLangService } from 'src/app/services/osm-lang.service';
import { GrailsBridgeService } from 'src/app/services/grails-bridge.service';
import { SharedService } from '../../services/sharedService';
import { of, empty } from 'rxjs';
import { By } from '@angular/platform-browser';
import { copyAnimationEvent } from '@angular/animations/browser/src/render/shared';


describe('ResultSelectionJobGroupComponent', () => {
  let component: ResultSelectionJobGroupComponent;
  let fixture: ComponentFixture<ResultSelectionJobGroupComponent>;
  const jobGroups = [new SelectableApplication({
    id: 3,
    name: 'test_Application',
    tags: ['test','application']
  }),
  new SelectableApplication({
    id: 1,
    name: 'test2_Application',
    tags: ['test2','application']
  })];

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

  it('should correctly show the tags according to the available job groups', () =>{
    component.jobGroupMappings$ = of(jobGroups);
    let tags: string[] = ['application','test','test2'];

    component.getJobGroupTags();
    expect(component.selectableTags.sort()).toEqual(tags);

    component.jobGroupMappings$ = of([]);
    component.getJobGroupTags();
    expect(component.selectableTags).toEqual([]);

  });

  it('should correctly show the job groups accordings to the selected tag',() => {
    component.jobGroupMappings$ = of(jobGroups);
    component.upadteJobGroups();
    component.getJobGroupTags();
    let tagJobGroupsMapping = getTagJobGroupsMapping(component.jobGroups, component.selectableTags);

    expect(component.filteredJobGroups).toEqual(component.jobGroups);

    component.filterByTag(component.selectableTags[0]);
    expect(component.filteredJobGroups).toEqual(tagJobGroupsMapping[0]);

    component.filterByTag(component.selectableTags[1]);
    expect(component.filteredJobGroups).toEqual(tagJobGroupsMapping[1]);

    component.filterByTag(component.selectableTags[2]);
    expect(component.filteredJobGroups).toEqual(tagJobGroupsMapping[2]);

    component.filterByTag(component.selectableTags[2]);
    expect(component.filteredJobGroups).toEqual(component.jobGroups);
  });

  it('should show the no result message', ()=>{
    const select = fixture.debugElement.query(By.css('select')).nativeElement;
    
    component.jobGroupMappings$ = of([]);
    component.upadteJobGroups();
    component.getJobGroupTags();
    fixture.detectChanges();

    expect(select.innerText.trim()).toEqual('frontend.de.iteratec.osm.resultSelection.jobGroup.noResults');
    
  })
});
function getTagJobGroupsMapping(jobGroups: SelectableApplication[], selectableTags: string[]): any{
  if(selectableTags){
    let sortedJobGroups = [];
    for(let i=1; i<=selectableTags.length; i++){
      sortedJobGroups.push([]);
    }
    selectableTags.forEach(tag =>{
    jobGroups.forEach(element => {
      if(element.tags.indexOf(tag) > -1){
        sortedJobGroups[selectableTags.indexOf(tag)].push(element);
        }
      })
    });
    return sortedJobGroups;
  }
}
