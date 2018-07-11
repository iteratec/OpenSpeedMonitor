import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ApplicationDTO} from "../../model/application.model";

@Component({
  selector: 'osm-application-select',
  templateUrl: './application-select.component.html',
  styleUrls: ['./application-select.component.scss']
})
export class ApplicationSelectComponent {
  @Output() selectedApplicationChange: EventEmitter<ApplicationDTO> = new EventEmitter();
  @Input() applications: ApplicationDTO[];
  @Input() selectedApplication: ApplicationDTO;

  constructor() {
  }

  setApplication(jobGroup: ApplicationDTO) {
    this.selectedApplicationChange.emit(jobGroup)
  }
}
