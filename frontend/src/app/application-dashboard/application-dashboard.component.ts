import {Component, OnDestroy} from '@angular/core';
import {JobGroupDTO} from '../shared/model/job-group.model';
import {ActivatedRoute, Router} from '@angular/router';
import {JobGroupService} from '../shared/service/rest/job-group.service';
import {combineLatest, Observable, Subject} from 'rxjs';
import {filter, map, takeUntil} from 'rxjs/operators';
import {ApplicationDashboardService} from './service/application-dashboard.service';

@Component({
  selector: 'osm-application-dashboard',
  templateUrl: './application-dashboard.component.html',
  styleUrls: ['./application-dashboard.component.css']
})
export class ApplicationDashboardComponent implements OnDestroy {
  jobGroups$: Observable<JobGroupDTO[]>;
  selectedApplication$: Observable<JobGroupDTO>;
  destroyed$ = new Subject<void>();

  constructor(private jobGroupService: JobGroupService, private route: ActivatedRoute, private router: Router, private dashboardService: ApplicationDashboardService) {
    this.jobGroups$ = jobGroupService.activeOrRecentlyMeasured$.pipe(
      map((jobGroups: JobGroupDTO[]) => this.sortJobGroupsByName(jobGroups))
    );

    this.handleValidNavigation();
    this.handleInvalidNavigation();
  }

  private handleValidNavigation() {
    this.selectedApplication$ = combineLatest(this.route.paramMap, this.jobGroups$).pipe(
      map(([params, jobGroups]) => this.lookForJobGroupWithId(jobGroups, params.get('jobGroupId'))),
      filter(jobGroup => !!jobGroup)
    );
    this.selectedApplication$.pipe(
      takeUntil(this.destroyed$)
    ).subscribe(selectedApplication => {
      this.dashboardService.updateMetricsForApplication(selectedApplication.id);
    });
  }

  ngOnDestroy() {
    this.destroyed$.next(null);
    this.destroyed$.complete();
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
    let selectedApplication = jobGroups.find(jobGroup => jobGroup.id == Number(jobGroupId));
    return selectedApplication
  }

  private sortJobGroupsByName(jobGroups: JobGroupDTO[]) {
    return jobGroups.sort((a, b) => a.name.localeCompare(b.name, [], {sensitivity: "base"}))
  }
}
