import {
    Component, Input,
    OnInit,
    ViewEncapsulation,
  } from '@angular/core';

import {Caller, ResultSelectionCommand} from "../../models/result-selection-command.model";
import {ResultSelectionService} from "../../services/result-selection.service";
import { SelectableApplication } from 'src/app/models/application.model';
import { Observable } from 'rxjs';
import { Chart } from '../../models/chart.model';
import { SharedService } from '../../services/sharedService';


@Component({
    selector: 'osm-result-selection-job-group',
    templateUrl: './result-selection-job-group.component.html',
    styleUrls: ['./result-selection-job-group.component.scss'],
    encapsulation: ViewEncapsulation.None
  })

export class ResultSelectionJobGroupComponent implements OnInit {
  @Input() currentChart: string;
  @Input() jobGroupMappings$: Observable<SelectableApplication[]>;  
  resultSelectionCommand: ResultSelectionCommand;
  jobGroups: SelectableApplication[];
  isEmpty = true;
  selectableTags: string[];
  filteredJobGroups: SelectableApplication[];
  selectedTag: string ='';
  isSelected = false;
    
  constructor(private resultSelectionService: ResultSelectionService, private sharedService: SharedService) {
    
  }
  
  ngOnInit() {
    let defaultFrom = new Date();
    let defaultTo = new Date();
    defaultTo.setHours(23, 59, 59, 999);
    defaultFrom.setDate(defaultTo.getDate() - 28);
    defaultFrom.setHours(0, 0, 0, 0);

    this.resultSelectionCommand = new ResultSelectionCommand({
      from: defaultFrom,
      to: defaultTo,
      caller: Caller.EventResult,
      jobGroupIds: [],
      pageIds: [],
      locationIds: [],
      browserIds: [],
      measuredEventIds: [],
      selectedConnectivities: [],
    }); 

    this.resultSelectionService.loadSelectableData(this.resultSelectionCommand,Chart[this.currentChart]);
    this.sharedService.currentMessage.subscribe(selectedDates => this.registerTimeFrameChangeEvents(selectedDates));
  }
    
  registerTimeFrameChangeEvents(dates: Date[]):void {
    this.resultSelectionCommand.from = dates[0];
    this.resultSelectionCommand.to = dates[1];
    this.resultSelectionService.loadSelectableApplications(this.resultSelectionCommand);
    this.jobGroupMappings$ = this.resultSelectionService.applications$;
    this.upadteJobGroups();
    this.getJobGroupTags();
  }

  upadteJobGroups() {
    this.jobGroupMappings$.subscribe(jobGroups => this.sortJobGroupsByName(jobGroups));
  }

  getJobGroupTags(){
    this.jobGroupMappings$.subscribe(next => {
      if(next){
        this.selectableTags = next.map(value => value.tags).reduce((a,b) => 
        a.concat(b), []).filter((v, i, a) => 
        a.indexOf(v) ===i);
      }
    });
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

  private sortJobGroupsByName(jobGroups: SelectableApplication[]): void{
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
      if(this.isSelected === true && (this.selectableTags.indexOf(this.selectedTag)>-1)){
        this.setFilteredJobGroups(this.selectedTag);
      }else{
        this.isSelected = false;
        this.filteredJobGroups = jobGroups;
      }
    }else{
      this.isEmpty=true;
    }
  }
}
