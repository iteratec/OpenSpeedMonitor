import {Component, EventEmitter, Input, Output} from '@angular/core';
import {IJobGroupToPagesMapping} from "../../common/model/job-group-to-page-mapping.model";
import {IPageId} from "../../common/model/page.model";
import {IPageComparisonSelection} from "../page-comparison-selection.model";

@Component({
  selector: 'page-comparison-row',
  templateUrl: './page-comparison-row.component.html'
})
export class PageComparisonRowComponent {
  @Input() jobGroupMappings: IJobGroupToPagesMapping[];
  @Input() selection: IPageComparisonSelection;
  @Input() removable: boolean;
  @Output() delete: EventEmitter<IPageComparisonSelection> = new EventEmitter();
  @Output() select: EventEmitter<IPageComparisonSelection> = new EventEmitter();

  constructor() {
  }

  removeComparison() {
    this.delete.emit(this.selection);
  }

  triggerComparisonChange() {
    this.select.emit(this.selection);
  }

  getPagesForJobGroup(id: number): IPageId[] {
    if (!this.jobGroupMappings) return [];
    const jobGroupMapping: IJobGroupToPagesMapping = this.jobGroupMappings.find(jobGroup => jobGroup.id == id);
    return jobGroupMapping ? jobGroupMapping.pages : []
  }
}
