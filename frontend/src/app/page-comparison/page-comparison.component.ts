import {Component, NgZone, OnInit} from '@angular/core';
import {IJobGroupToPagesMapping} from "../common/model/job-group-to-page-mapping.model";
import {PageComparisonSelection} from "./page-comparison-selection.model";
import {JobGroupRestService} from "../setup-dashboard/service/rest/job-group-rest.service";

@Component({
  selector: 'page-comparison',
  templateUrl: './page-comparison.component.html'
})
export class PageComparisonComponent implements OnInit {
  jobGroupMappings: IJobGroupToPagesMapping[] = [];
  pageComparisonSelections: PageComparisonSelection[] = [];
  canRemoveRow: boolean = false;

  constructor(private jobGroupService:JobGroupRestService, private zone: NgZone) {
    this.exposeComponent();
  }

  exposeComponent(){
    window['pageComparisonComponent'] = {
      zone: this.zone,
      getSelectedPages: (value) => this.getSelectedPages(),
      component: this,
    };
  }

  getSelectedPages(){
    return this.pageComparisonSelections;
  }

  ngOnInit() {
    this.addComparison();
    this.registerTimeFrameChangeEvent();
  }

  checkIfDelete(){
    this.canRemoveRow = this.pageComparisonSelections.length>1;
  }

  onDelete(event:PageComparisonSelection){
    let index:number = this.pageComparisonSelections.indexOf(event);
    if(index>=0){
      this.pageComparisonSelections.splice(index,1)
    }
    this.checkIfDelete();
  }

  registerTimeFrameChangeEvent() {
    document.getElementById("select-interval-timeframe-card").addEventListener("timeFrameChanged", (event: any) => {
      this.getJobGroups(event.detail[0].toISOString(), event.detail[1].toISOString());
    })
  }

  addComparison() {
    this.pageComparisonSelections.push(new PageComparisonSelection());
    this.checkIfDelete();
  }

  getJobGroups(from: string, to: string) {
    this.jobGroupService.getJobGroupToPagesMap(from, to).subscribe(next => this.jobGroupMappings = next)
  }
}
