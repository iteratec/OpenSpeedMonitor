import {
    Component, Input,
    ViewEncapsulation,
  } from '@angular/core';

import {ResultSelectionService} from "../../services/result-selection.service";
import { SelectableApplication } from 'src/app/models/application.model';
import { Observable } from 'rxjs';


@Component({
    selector: 'osm-result-selection-job-group',
    templateUrl: './result-selection-job-group.component.html',
    styleUrls: ['./result-selection-job-group.component.scss'],
    encapsulation: ViewEncapsulation.None
  })

export class ResultSelectionJobGroupComponent {
  @Input() currentChart: string;
  @Input() jobGroupMappings$: Observable<SelectableApplication[]>;  
  resultSelectionCommand: ResultSelectionCommand;
  jobGroups: SelectableApplication[];
  isEmpty = true;
  selectableTags: string[];
  filteredJobGroups: SelectableApplication[];
  selectedTag: string ='';
  isSelected = false;
    
  constructor(private resultSelectionService: ResultSelectionService) {
    this.jobGroupMappings$ = this.resultSelectionService.applications$;

    this.jobGroupMappings$.subscribe(jobGroups => this.updateJobGroupsAndTags(jobGroups));
  }

  filterByTag(tag: string): void{
    if(this.isSelected === false){
      this.isSelected = true;
      this.selectedTag = tag;
      this.setFilteredJobGroups(this.selectedTag);
    }else{
        if(tag!==this.selectedTag){
        this.selectedTag = tag;
        this.setFilteredJobGroups(this.selectedTag);
      }else{
        this.isSelected =false;
        this.filteredJobGroups = this.jobGroups;
      }
    }
  }

  updateJobGroupsAndTags(jobGroups: SelectableApplication[]): void{
    if(jobGroups!=null && jobGroups.length>0){
      this.isEmpty=false;
      jobGroups.sort((a, b) => {
        if(a.name.toLowerCase() > b.name.toLowerCase()){
          return 1;
        }
        if(a.name.toLowerCase() < b.name.toLowerCase()){
          return -1;
        }
        return 0;
        });
      this.jobGroups = jobGroups;

      this.updateTags(this.jobGroups);

      if(this.isSelected === true && (this.selectableTags.indexOf(this.selectedTag)>-1)){
        this.setFilteredJobGroups(this.selectedTag);
      }else{
        this.isSelected = false;
        this.filteredJobGroups = jobGroups;
      }
    }else{
      this.isEmpty=true;
      this.updateTags(jobGroups);
    }
  }

  private updateTags(jobGroups: SelectableApplication[]){
    if(jobGroups){
      this.selectableTags = jobGroups.map(value => value.tags).reduce((a,b) => 
         a.concat(b), []).filter((v, i, a) => 
         a.indexOf(v) ===i);
    }else{
      this.selectableTags = [];
    }
  }

  private setFilteredJobGroups(tag: string): void{
    let filteredJobGroups = [];
    if(this.jobGroups){
      this.jobGroups.forEach(element => {
        if(element.tags.indexOf(tag) > -1){
          filteredJobGroups.push(element);
        }
      });
      this.filteredJobGroups = filteredJobGroups;
    }
  }
}
