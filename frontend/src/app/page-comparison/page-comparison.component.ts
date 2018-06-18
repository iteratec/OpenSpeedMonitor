import {Component} from '@angular/core';
import {IJobGroupToPagesMapping} from "../common/model/job-group-to-page-mapping.model";
import {IPageComparisonSelection} from "./page-comparison-selection.model";
import {JobGroupService} from "../setup-dashboard/service/rest/job-group.service";
import {log} from "util";

@Component({
  selector: 'page-comparison',
  templateUrl: './page-comparison.component.html'
})
export class PageComparisonComponent {
  jobGroupToPagesMapping: IJobGroupToPagesMapping[] = [];
  pageComparisonSelections: IPageComparisonSelection[] = [];
  canRemoveRow: boolean = false;

  constructor(private jobGroupService: JobGroupService) {
    this.addComparison();
    this.registerTimeFrameChangeEvent();
  }

  checkIfRowsAreRemovable() {
    this.canRemoveRow = this.pageComparisonSelections.length > 1;
  }

  onComparisonRowRemove(event: IPageComparisonSelection) {
    let index: number = this.pageComparisonSelections.indexOf(event);
    if (index >= 0) {
      this.pageComparisonSelections.splice(index, 1)
    }
    this.checkIfRowsAreRemovable();
  }

  validateComparisons() {
    window.dispatchEvent(new Event("historyStateChanged"));
    let isValid = !this.pageComparisonSelections.find(comparison => !this.isComparisonValid(comparison));
    window.dispatchEvent(new CustomEvent("pageComparisonValidation", {detail: {isValid: isValid}}))
  }

  isComparisonValid(comparison: IPageComparisonSelection) {
    return comparison.firstJobGroupId !== -1 && comparison.secondJobGroupId !== -1 && comparison.firstPageId !== -1 && comparison.secondPageId !== -1;
  }


  registerTimeFrameChangeEvent() {
    //TimeFrame is currently not a angular component, so we have to do it "manually"
    document.getElementById("select-interval-timeframe-card").addEventListener("timeFrameChanged", (event: any) => {
      this.getJobGroups(event.detail[0].toISOString(), event.detail[1].toISOString());
    })
  }

  addComparison() {
    this.pageComparisonSelections.push(<IPageComparisonSelection>{
      firstJobGroupId: -1,
      firstPageId: -1,
      secondPageId: -1,
      secondJobGroupId: -1
    });
    this.checkIfRowsAreRemovable();
    this.validateComparisons();
  }

  getJobGroups(from: string, to: string) {
    this.jobGroupService.getJobGroupToPagesMap(from, to).subscribe(next => this.jobGroupToPagesMapping = next)
  }
}
