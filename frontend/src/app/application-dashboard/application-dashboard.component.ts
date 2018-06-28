import {Component} from '@angular/core';
import {JobGroupDTO} from "../shared/model/job-group.model";
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'osm-application-dashboard',
  templateUrl: './application-dashboard.component.html',
  styleUrls: ['./application-dashboard.component.css']
})
export class ApplicationDashboardComponent {
  application: JobGroupDTO;
  jobGroupId: number;

  constructor(private route: ActivatedRoute, private router: Router) {
    this.route.paramMap.subscribe(params => {
      this.jobGroupId = +params.get('jobGroupId');
    });
  }

  updateApplication(jobGroup: JobGroupDTO) {
    this.router.navigate(['/application-dashboard', jobGroup.id]);
    this.application = jobGroup
  }
}
