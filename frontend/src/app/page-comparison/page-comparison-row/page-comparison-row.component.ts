import {Component, EventEmitter, Input, Output} from '@angular/core';
import {PageComparisonSelectionDto} from "../page-comparison-selection.model";
import {JobGroupToPagesMappingDto} from "../model/job-group-to-page-mapping.model";
import {PageDto} from "../model/page.model";

@Component({
  selector: 'osm-page-comparison-row',
  templateUrl: './page-comparison-row.component.html'
})
export class PageComparisonRowComponent {
  @Input() jobGroupMappings: JobGroupToPagesMappingDto[];
  @Input() selection: PageComparisonSelectionDto;
  @Input() removable: boolean;
  @Output() delete: EventEmitter<PageComparisonSelectionDto> = new EventEmitter();
  @Output() select: EventEmitter<PageComparisonSelectionDto> = new EventEmitter();

  constructor() {
  }

  removeComparison() {
    this.delete.emit(this.selection);
  }

  triggerComparisonChange() {
    this.select.emit(this.selection);
  }

  getPagesForJobGroup(id: number): PageDto[] {
    if (!this.jobGroupMappings) return [];
    const jobGroupMapping: JobGroupToPagesMappingDto = this.jobGroupMappings.find(jobGroup => jobGroup.id == id);
    return jobGroupMapping ? jobGroupMapping.pages : []
  }
}
