import {Component} from '@angular/core';
import {JobGroupDTO} from "../../common/model/job-group.model";
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'osm-application-dashboard',
  templateUrl: './application-dashboard.component.html',
  styleUrls: ['./application-dashboard.component.css']
})
export class ApplicationDashboardComponent {
  application: JobGroupDTO;

  constructor(private route: ActivatedRoute, private router: Router) {
    route.params.subscribe(params => console.log("jobGroupId ", params));
    route.data.subscribe(params => console.log("data ", params));
    route.url.subscribe(params => console.log("url ", params));
    console.log("config ", route.routeConfig);
    console.log("rootPath ", route.pathFromRoot);
  }

  updateApplication(jobGroup: JobGroupDTO) {
    this.application = jobGroup
  }
}
