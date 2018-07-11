import {Component, OnDestroy} from '@angular/core';
import {JobGroupDTO} from '../shared/model/job-group.model';
import {ActivatedRoute, Router} from '@angular/router';
import {JobGroupService} from '../shared/service/rest/job-group.service';
import {combineLatest, Observable, Subject} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import {ApplicationDashboardService} from './service/application-dashboard.service';

@Component({
  selector: 'osm-application-dashboard',
  templateUrl: './application-dashboard.component.html',
  styleUrls: ['./application-dashboard.component.scss']
})
export class ApplicationDashboardComponent implements OnDestroy {
  jobGroups$: Observable<JobGroupDTO[]>;
  selectedApplication: JobGroupDTO;
  destroyed$ = new Subject<void>();

  constructor(private jobGroupService: JobGroupService, private route: ActivatedRoute, private router: Router, private dashboardService: ApplicationDashboardService) {
    this.jobGroups$ = jobGroupService.activeOrRecentlyMeasured$.pipe(
      map((jobGroups: JobGroupDTO[]) => this.sortJobGroupsByName(jobGroups))
    );

    combineLatest(this.route.paramMap, this.jobGroups$)
      .pipe(takeUntil(this.destroyed$))
      .subscribe(([params, jobGroups]) => this.handleNavigation(params.get('jobGroupId'), jobGroups));
  }

  private handleNavigation(jobGroupId: string, jobGroups: JobGroupDTO[]) {
    if (!jobGroupId) {
      this.updateApplication(jobGroups[0]);
      return;
    }
    this.selectedApplication = this.findJobGroupById(jobGroups, jobGroupId);
    if (this.selectedApplication) {
      this.dashboardService.updateMetricsForApplication(this.selectedApplication.id);
    }
  }

  ngOnDestroy() {
    this.destroyed$.next(null);
    this.destroyed$.complete();
  }

  updateApplication(jobGroup: JobGroupDTO) {
    this.router.navigate(['/application-dashboard', jobGroup.id]);
  }

  private findJobGroupById(jobGroups: JobGroupDTO[], jobGroupId: string) {
    return jobGroups.find(jobGroup => jobGroup.id == Number(jobGroupId));
  }

  private sortJobGroupsByName(jobGroups: JobGroupDTO[]) {
    return jobGroups.sort((a, b) => a.name.localeCompare(b.name, [], {sensitivity: "base"}))
  }
}
