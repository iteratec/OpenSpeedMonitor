import {Component} from '@angular/core';
import {JobGroupDTO} from "../shared/model/job-group.model";
import {ApplicationDashboardService} from "./service/application-dashboard.service";

@Component({
  selector: 'osm-application-dashboard',
  templateUrl: './application-dashboard.component.html',
  styleUrls: ['./application-dashboard.component.css']
})
export class ApplicationDashboardComponent {
  application: JobGroupDTO;

  constructor(private dashboardService: ApplicationDashboardService) {
  }

  updateApplication(jobGroup: JobGroupDTO) {
    this.application = jobGroup;
    this.dashboardService.updateMetricsForApplication(jobGroup.id);
  }
}
