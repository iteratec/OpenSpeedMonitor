import {Component, OnInit} from '@angular/core';
import {JobGroupRestService} from "./service/rest/job-group-rest.service";

@Component({
  selector: 'app-setup-dashboard',
  templateUrl: './setup-dashboard.component.html',
  styleUrls: ['./setup-dashboard.component.css']
})
export class SetupDashboardComponent implements OnInit {
  activeJobs: object;

  constructor(private jobGroupRestService: JobGroupRestService) {
  }

  ngOnInit() {
    this.getActiveJobs();
  }

  getActiveJobs() {
    this.jobGroupRestService.getActiveJobGroups().subscribe((activeJobs) => {
      this.activeJobs = activeJobs;
    })
  }
}
