import {JobGroupToPagesMappingDto} from "../shared/models/job-group-to-page-mapping.model";
import {Component, NgZone} from '@angular/core';
import {PageComparisonSelectionDto} from "./models/page-comparison-selection.model";
import {JobGroupService} from "../shared/services/rest/job-group.service";
import {Observable} from "rxjs/internal/Observable";
import {PageComparisonComponentAdapter} from "./page-comparison.adapter";

@Component({
  selector: 'osm-page-comparison',
  templateUrl: './page-comparison.component.html'
})
export class PageComparisonComponent {
  jobGroupToPagesMapping$: Observable<JobGroupToPagesMappingDto[]>;
  pageComparisonSelections: PageComparisonSelectionDto[] = [];
  canRemoveRow: boolean = false;
  pageComparisonComponentAdapter: PageComparisonComponentAdapter;

  constructor(private jobGroupService: JobGroupService, zone: NgZone) {
    this.addComparison();
    this.registerTimeFrameChangeEvent();
    this.pageComparisonComponentAdapter = new PageComparisonComponentAdapter(zone, this);
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
    this.jobGroupToPagesMapping$ = this.jobGroupService.getJobGroupToPagesMapDto(from, to);
  }
}
