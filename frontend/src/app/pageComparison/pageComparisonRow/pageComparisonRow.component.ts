import {Component, Input, OnInit} from '@angular/core';
import {JobGroup} from "../../setup-dashboard/model/job-group.model";
import {JobGroupToPagesMapping} from "../service/JobGroupToPagesMapping";
import {PageId} from "../service/PageId";
import {isNullOrUndefined} from "util";
import {PageComparisonSelection} from "../PageComparisonSelection";

@Component({
  selector: 'page-comparison-row',
  templateUrl: './pageComparisonRow.component.html'
  // styleUrls: ['./.component.css']
})
export class PageComparisonRowComponent implements OnInit {
  @Input() jobGroupMappings:JobGroupToPagesMapping[];
  @Input() selection:PageComparisonSelection;

  constructor() {
  }

  getPagesForJobGroup(id:string):PageId[]{
      let jobGroupMapping:JobGroupToPagesMapping = this.jobGroupMappings.find(jobGroup=> jobGroup.id==id)
      if (isNullOrUndefined(jobGroupMapping)){
        return []
      } else {
        return jobGroupMapping.pages
      }
  }

  setFirstSelectedJobGroup(id:string){
    this.selection.firstJobGroupId = id;
  }

  setSecondSelectedJobGroup(id:string){
    this.selection.secondJobGroupId = id;
  }
  setFirstSelectedPage(id:string){
    this.selection.firstPageId = id;
  }
  setSecondSelectedPage(id:string){
    this.selection.secondPageId= id;
  }
  ngOnInit() {
  }

}
