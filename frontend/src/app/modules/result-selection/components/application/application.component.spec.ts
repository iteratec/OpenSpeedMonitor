import { async, ComponentFixture, TestBed} from '@angular/core/testing';
import { ApplicationComponent } from './application.component';
import { SelectableApplication } from 'src/app/models/application.model';
import { SharedMocksModule } from 'src/app/testing/shared-mocks.module';
import { OsmLangService } from 'src/app/services/osm-lang.service';
import { GrailsBridgeService } from 'src/app/services/grails-bridge.service';
import { By } from '@angular/platform-browser';
import {ResultSelectionStore} from "../../services/result-selection.store";
import {ResultSelectionService} from "../../services/result-selection.service";

describe('ApplicationComponent', () => {
  let component: ApplicationComponent;
  let fixture: ComponentFixture<ApplicationComponent>;
  const applications = [new SelectableApplication({
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
      declarations: [ApplicationComponent],
      imports: [SharedMocksModule],
      providers: [
        ResultSelectionStore,
        OsmLangService,
        GrailsBridgeService,
        ResultSelectionService
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationComponent);
    
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should correctly show the tags according to the available job groups', () =>{
    let tags: string[] = ['application','test','test2'];

    component.updateApplicationsAndTags(applications);
    expect(component.selectableTags.sort()).toEqual(tags);

    component.updateApplicationsAndTags([]);
    expect(component.selectableTags).toEqual([]);

  });

  it('should correctly show the job groups according to the selected tag',() => {
    component.updateApplicationsAndTags(applications);
    let tagApplicationsMapping = getTagApplicationsMapping(component.applications, component.selectableTags);

    expect(component.filteredApplications).toEqual(component.applications);

    component.selectTag(component.selectableTags[0]);
    expect(component.filteredApplications).toEqual(tagApplicationsMapping[0]);

    component.selectTag(component.selectableTags[1]);
    expect(component.filteredApplications).toEqual(tagApplicationsMapping[1]);

    component.selectTag(component.selectableTags[2]);
    expect(component.filteredApplications).toEqual(tagApplicationsMapping[2]);

    component.selectTag(component.selectableTags[2]);
    expect(component.filteredApplications).toEqual(component.applications);
  });

  it('should show the no result message', ()=>{
    const select = fixture.debugElement.query(By.css('select > option')).nativeElement;
    
    component.updateApplicationsAndTags([]);
    fixture.detectChanges();

    expect(select.innerText.trim()).toEqual('frontend.de.iteratec.osm.resultSelection.application.noResults');
    
  })
});
function getTagApplicationsMapping(jobGroups: SelectableApplication[], selectableTags: string[]): any{
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
