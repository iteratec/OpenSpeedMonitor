import {Component} from '@angular/core';
import {JobGroupToPagesMappingDto} from "../common/model/job-group-to-page-mapping.model";
import {PageComparisonSelectionDto} from "./page-comparison-selection.model";
import {JobGroupRestService} from "../setup-dashboard/service/rest/job-group-rest.service";
import {Observable} from "rxjs/internal/Observable";

@Component({
  selector: 'page-comparison',
  templateUrl: './page-comparison.component.html'
})
export class PageComparisonComponent {
  jobGroupToPagesMapping: Observable<JobGroupToPagesMappingDto[]>;
  pageComparisonSelections: PageComparisonSelectionDto[] = [];
  canRemoveRow: boolean = false;

  constructor(private jobGroupService: JobGroupRestService) {
    this.addComparison();
    this.registerTimeFrameChangeEvent();
  }

  checkIfRowsAreRemovable() {
    this.canRemoveRow = this.pageComparisonSelections.length > 1;
  }

  onComparisonRowRemove(event: PageComparisonSelectionDto) {
    let index: number = this.pageComparisonSelections.indexOf(event);
    if (index >= 0) {
      this.pageComparisonSelections.splice(index, 1)
    }
    this.checkIfRowsAreRemovable();
  }

  validateComparisons() {
    let isValid = !this.pageComparisonSelections.find(comparison => !this.isComparisonValid(comparison));
    if (isValid) window.dispatchEvent(new Event("historyStateChanged"));
    window.dispatchEvent(new CustomEvent("pageComparisonValidation", {detail: {isValid: isValid}}))
  }

  isComparisonValid(comparison: PageComparisonSelectionDto) {
    return comparison.firstJobGroupId !== -1 && comparison.secondJobGroupId !== -1 && comparison.firstPageId !== -1 && comparison.secondPageId !== -1;
  }


  registerTimeFrameChangeEvent() {
    //TimeFrame is currently not a angular component, so we have to do it "manually"
    document.getElementById("select-interval-timeframe-card").addEventListener("timeFrameChanged", (event: any) => {
      this.getJobGroups(event.detail[0].toISOString(), event.detail[1].toISOString());
    })
  }

  addComparison() {
    this.pageComparisonSelections.push(<PageComparisonSelectionDto>{
      firstJobGroupId: -1,
      firstPageId: -1,
      secondPageId: -1,
      secondJobGroupId: -1
    });
    this.checkIfRowsAreRemovable();
    this.validateComparisons();
  }

  getJobGroups(from: string, to: string) {
    this.jobGroupToPagesMapping = this.jobGroupService.getJobGroupToPagesMapDto(from, to);
  }
}
