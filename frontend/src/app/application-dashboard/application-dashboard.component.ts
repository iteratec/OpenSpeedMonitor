import {Component, OnDestroy} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable, Subject} from 'rxjs';
import {filter, map, takeUntil} from 'rxjs/operators';
import {ApplicationDashboardService} from './service/application-dashboard.service';
import {ApplicationDTO} from "./model/application.model";

@Component({
  selector: 'osm-application-dashboard',
  templateUrl: './application-dashboard.component.html',
  styleUrls: ['./application-dashboard.component.scss']
})
export class ApplicationDashboardComponent implements OnDestroy {
  jobGroups$: Observable<ApplicationDTO[]>;
  selectedApplication$: Observable<ApplicationDTO>;
  destroyed$ = new Subject<void>();

  constructor(private route: ActivatedRoute, private router: Router, private dashboardService: ApplicationDashboardService) {
    this.jobGroups$ = dashboardService.activeOrRecentlyMeasured$.pipe(
      map((jobGroups: ApplicationDTO[]) => this.sortJobGroupsByName(jobGroups))
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

  updateApplication(jobGroup: ApplicationDTO) {
    this.router.navigate(['/application-dashboard', jobGroup.id]);
  }

  private lookForJobGroupWithId(jobGroups: ApplicationDTO[], jobGroupId: string) {
    let selectedApplication = jobGroups.find(jobGroup => jobGroup.id == Number(jobGroupId));
    return selectedApplication
  }

  private sortJobGroupsByName(jobGroups: ApplicationDTO[]) {
    return jobGroups.sort((a, b) => a.name.localeCompare(b.name, [], {sensitivity: "base"}))
  }
}
