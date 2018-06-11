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

  constructor(private jobGroupService: JobGroupRestService, private zone: NgZone) {
    this.exposeComponent();
  }

  exposeComponent() {
    window['pageComparisonComponent'] = {
      zone: this.zone,
      getSelectedPages: () => this.getSelectedPages(),
      component: this,
    };
  }

  getSelectedPages() {
    return this.pageComparisonSelections;
  }

  ngOnInit() {
    this.addComparison();
    this.disableShowButton();
    this.registerTimeFrameChangeEvent();
  }

  checkIfDelete() {
    this.canRemoveRow = this.pageComparisonSelections.length > 1;
  }

  onDelete(event: PageComparisonSelection) {
    let index: number = this.pageComparisonSelections.indexOf(event);
    if (index >= 0) {
      this.pageComparisonSelections.splice(index, 1)
    }
    this.checkIfDelete();
  }

  onChange(selection: PageComparisonSelection) {
    if (selection.isValid()) {
      this.enableShowButton();
    } else {
      this.disableShowButton();
    }
  }

  disableShowButton() {
    const button = document.getElementById("graphButtonHtmlId");
    button.setAttribute("disabled", "disabled");
    document.getElementById('warning-no-page').style.display = 'block'
  }

  enableShowButton() {
    const button = document.getElementById("graphButtonHtmlId");
    button.removeAttribute("disabled");
    document.getElementById('warning-no-page').style.display = 'none'
  }

  registerTimeFrameChangeEvent() {
    //TimeFrame is currently not a angular component, so we have to do it "manually"
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
