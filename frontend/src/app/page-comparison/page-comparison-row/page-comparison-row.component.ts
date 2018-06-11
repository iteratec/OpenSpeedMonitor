import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {IJobGroupToPagesMapping} from "../../common/model/job-group-to-page-mapping.model";
import {IPageId} from "../../common/model/page.model";
import {isNullOrUndefined} from "util";
import {IPageComparisonSelection} from "../page-comparison-selection.model";

@Component({
  selector: 'page-comparison-row',
  templateUrl: './page-comparison-row.component.html'
  // styleUrls: ['./.component.css']
})
export class PageComparisonRowComponent{
  @Input() jobGroupMappings: IJobGroupToPagesMapping[];
  @Input() selection: IPageComparisonSelection;
  @Input() removable: boolean;
  @Output() deleteEmitter: EventEmitter<IPageComparisonSelection> = new EventEmitter();
  @Output() changeEmitter: EventEmitter<IPageComparisonSelection> = new EventEmitter();

  constructor() {
  }

  removeComparison(){
    this.deleteEmitter.emit(this.selection);
  }

  triggerComparisonChange(){
    this.changeEmitter.emit(this.selection);
  }

  getPagesForJobGroup(id: number): IPageId[] {
    let jobGroupMapping: IJobGroupToPagesMapping = this.jobGroupMappings.find(jobGroup => jobGroup.id == id);
    if (isNullOrUndefined(jobGroupMapping)) {
      return []
    } else {
      return jobGroupMapping.pages
    }
  }

  setFirstSelectedJobGroup(id: number) {
    this.selection.firstJobGroupId = id;
    this.triggerComparisonChange();
  }

  setSecondSelectedJobGroup(id: number) {
    this.selection.secondJobGroupId = id;
    this.triggerComparisonChange();
  }

  setFirstSelectedPage(id: number) {
    this.selection.firstPageId = id;
    this.triggerComparisonChange();
  }

  setSecondSelectedPage(id: number) {
    this.selection.secondPageId = id;
    this.triggerComparisonChange();
  }
}
