import {
    Component,
    ViewEncapsulation,
  } from '@angular/core';

import { SelectableApplication } from 'src/app/models/application.model';
import {Observable} from 'rxjs';
import {ResultSelectionStore} from "../../services/result-selection.store";


@Component({
    selector: 'osm-result-selection-application',
    templateUrl: './application.component.html',
    styleUrls: ['./application.component.scss'],
    encapsulation: ViewEncapsulation.None
  })

export class ApplicationComponent {
  applicationMappings$: Observable<SelectableApplication[]>;
  applications: SelectableApplication[];
  isEmpty = true;
  selectableTags: string[];
  filteredApplications: SelectableApplication[];
  selectedTag: string ='';
  isSelected = false;
  selectedApplications: number[] = [];
   selectedComponent: string = "APPLICATION";

  constructor(private resultSelectionStore: ResultSelectionStore) {
      this.resultSelectionStore.resultSelectionCommandListener(this.selectedComponent);
      this.applicationMappings$ = this.resultSelectionStore.applications$;
      this.applicationMappings$.subscribe(applications => {
        if(applications){
        this.updateApplicationsAndTags(applications);
        }
      });
  }

  filterByTag(tag: string): void{
    if(this.isSelected === false){
      this.isSelected = true;
      this.selectedTag = tag;
      this.setFilteredApplications(this.selectedTag);
    }else{
        if(tag!==this.selectedTag){
        this.selectedTag = tag;
        this.setFilteredApplications(this.selectedTag);
      }else{
        this.isSelected =false;
        this.filteredApplications = this.applications;
      }
    }
  }

  updateApplicationsAndTags(applications: SelectableApplication[]): void{
    if(applications!=null && applications.length>0){
      this.isEmpty=false;
      applications.sort((a, b) => {
        return a.name.localeCompare(b.name);
      });
      this.applications = applications;

      this.updateTags(this.applications);

      if(this.isSelected === true && (this.selectableTags.indexOf(this.selectedTag)>-1)){
        this.setFilteredApplications(this.selectedTag);
      }else{
        this.isSelected = false;
        this.filteredApplications = applications;
        this.selectedApplications = this.selectedApplications.filter(item => applications.map(item => item.id).includes(item));
      }
    }else{
      this.isEmpty=true;
      this.updateTags(applications);
    }
  }

  private updateTags(applications: SelectableApplication[]){
    if(applications){
      this.selectableTags = applications.map(value => value.tags).reduce((a,b) =>
         a.concat(b), []).filter((v, i, a) => 
         a.indexOf(v) ===i);
    }else{
      this.selectableTags = [];
    }
  }

  private setFilteredApplications(tag: string): void{
    let filteredJobGroups = [];
    if(this.applications){
      this.applications.forEach(element => {
        if(element.tags.indexOf(tag) > -1){
          filteredJobGroups.push(element);
        }
      });
      this.selectedApplications = this.selectedApplications.filter(item => filteredJobGroups.map(item => item.id).includes(item));
      this.filteredApplications = filteredJobGroups;
    }
    this.resultSelectionStore.setSelectedJobGroups(this.selectedApplications);
  }

  onSelectionChange(){
    if(this.selectedApplications){
      this.resultSelectionStore.setSelectedJobGroups(this.selectedApplications);
    }
  }
}
