import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {IJobGroupToPagesMapping} from "../../common/model/job-group-to-page-mapping.model";
import {IPageId} from "../../common/model/page.model";
import {isNullOrUndefined} from "util";
import {PageComparisonSelection} from "../page-comparison-selection.model";

@Component({
  selector: 'page-comparison-row',
  templateUrl: './page-comparison-row.component.html'
  // styleUrls: ['./.component.css']
})
export class PageComparisonRowComponent implements OnInit {
  @Input() jobGroupMappings: IJobGroupToPagesMapping[];
  @Input() selection: PageComparisonSelection;
  @Input() removable: boolean;
  @Output() deleteEmitter: EventEmitter<PageComparisonSelection> = new EventEmitter();

  constructor() {
  }

  onDelete(){
    this.deleteEmitter.emit(this.selection);
  }

  getPagesForJobGroup(id: string): IPageId[] {
    let jobGroupMapping: IJobGroupToPagesMapping = this.jobGroupMappings.find(jobGroup => jobGroup.id == id);
    if (isNullOrUndefined(jobGroupMapping)) {
      return []
    } else {
      return jobGroupMapping.pages
    }
  }

  setFirstSelectedJobGroup(id: string) {
    this.selection.firstJobGroupId = id;
  }

  setSecondSelectedJobGroup(id: string) {
    this.selection.secondJobGroupId = id;
  }

  setFirstSelectedPage(id: string) {
    this.selection.firstPageId = id;
  }

  setSecondSelectedPage(id: string) {
    this.selection.secondPageId = id;
  }

  ngOnInit() {
  }

}
