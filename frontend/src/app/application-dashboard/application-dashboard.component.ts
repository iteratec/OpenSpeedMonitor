import {Component} from '@angular/core';
import {JobGroupDTO} from "../shared/model/job-group.model";
import {ActivatedRoute, Router} from '@angular/router';
import {JobGroupService} from "../shared/service/rest/job-group.service";
import {combineLatest, Observable} from 'rxjs';
import {filter, map} from 'rxjs/operators';

@Component({
  selector: 'osm-application-dashboard',
  templateUrl: './application-dashboard.component.html',
  styleUrls: ['./application-dashboard.component.scss']
})
export class ApplicationDashboardComponent {
  jobGroups$: Observable<JobGroupDTO[]>;
  selectedApplication$: Observable<JobGroupDTO>;

  constructor(private jobGroupService: JobGroupService, private route: ActivatedRoute, private router: Router) {
    this.jobGroups$ = jobGroupService.activeOrRecentlyMeasured$.pipe(
      map((jobGroups: JobGroupDTO[]) => this.sortJobGroupsByName(jobGroups))
    );

    this.selectedApplication$ = combineLatest(this.jobGroups$, this.route.paramMap).pipe(
      map(([jobGroups, params]) => this.lookForJobGroupWithId(jobGroups, params.get('jobGroupId'))),
      filter(jobGroup => !!jobGroup)
    );

    this.handleInvalidNavigation();
  }

  private handleInvalidNavigation() {
    combineLatest(this.jobGroups$, this.route.paramMap).subscribe(([jobGroups, params]) => {
      if (this.lookForJobGroupWithId(jobGroups, params.get('jobGroupId'))) {
        return;
      }
      if (!params.get('jobGroupId')) {
        this.updateApplication(jobGroups[0])
      }
    });
  }

  updateApplication(jobGroup: JobGroupDTO) {
    this.router.navigate(['/application-dashboard', jobGroup.id]);
  }

  private lookForJobGroupWithId(jobGroups: JobGroupDTO[], jobGroupId: string) {
    return jobGroups.find(jobGroup => jobGroup.id == Number(jobGroupId));
  }

  private sortJobGroupsByName(jobGroups: JobGroupDTO[]) {
    return jobGroups.sort((a, b) => a.name.localeCompare(b.name, [], {sensitivity: "base"}))
  }
}
