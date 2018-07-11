import {Component, EventEmitter, Input, Output} from '@angular/core';
import {JobGroupDTO} from "../../../shared/models/job-group.model";

@Component({
  selector: 'osm-application-select',
  templateUrl: './application-select.component.html',
  styleUrls: ['./application-select.component.scss']
})
export class ApplicationSelectComponent {
  @Output() selectedApplicationChange: EventEmitter<JobGroupDTO> = new EventEmitter();
  @Input() applications: JobGroupDTO[];
  @Input() selectedApplication: JobGroupDTO;

  constructor() {
  }

  setApplication(jobGroup: JobGroupDTO) {
    this.selectedApplicationChange.emit(jobGroup)
  }
}
