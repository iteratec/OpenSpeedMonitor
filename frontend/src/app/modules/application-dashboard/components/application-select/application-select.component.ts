import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Application, ApplicationDTO} from "../../../../models/application.model";

@Component({
  selector: 'osm-application-select',
  templateUrl: './application-select.component.html',
  styleUrls: ['./application-select.component.scss']
})
export class ApplicationSelectComponent {
  @Output() selectedApplicationChange: EventEmitter<Application> = new EventEmitter();
  @Input() applications: Application[];
  @Input() selectedApplication: Application;

  constructor() {
  }

  setApplication(application: Application) {
    this.selectedApplicationChange.emit(application)
  }
}
