import {Component} from '@angular/core';
import {JobGroupDTO} from "../shared/model/job-group.model";

@Component({
  selector: 'osm-application-dashboard',
  templateUrl: './application-dashboard.component.html',
  styleUrls: ['./application-dashboard.component.css']
})
export class ApplicationDashboardComponent {
  application: JobGroupDTO;

  constructor() { }

  updateApplication(jobGroup: JobGroupDTO) {
    this.application = jobGroup
  }
}
