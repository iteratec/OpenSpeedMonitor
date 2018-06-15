import {Component, OnInit} from '@angular/core';
import {JobGroupRestService} from "./service/rest/job-group-rest.service";
import {JobGroup, JobGroupFromJson} from "./service/rest/job-group.model";

@Component({
  selector: 'app-setup-dashboard',
  templateUrl: './setup-dashboard.component.html',
  styleUrls: ['./setup-dashboard.component.css']
})
export class SetupDashboardComponent implements OnInit {
  activeJobGroups: JobGroup[];

  constructor(private jobGroupRestService: JobGroupRestService) {
  }

  ngOnInit() {
    this.getActiveJobs();
  }

  getActiveJobs() {
    this.jobGroupRestService.getActiveJobGroups().subscribe((activeJobs: any[]) => {
      this.activeJobGroups = activeJobs.map(jobJson => new JobGroupFromJson(jobJson));
    })
  }
}
