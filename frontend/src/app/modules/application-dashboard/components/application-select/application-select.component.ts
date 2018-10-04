import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ApplicationDTO} from "../../models/application.model";

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

  setApplication(application: ApplicationDTO) {
    this.selectedApplicationChange.emit(application)
  }
}
