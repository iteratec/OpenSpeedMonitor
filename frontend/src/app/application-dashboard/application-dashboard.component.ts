import {Component, OnDestroy} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable, Subject} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import {ApplicationDashboardService} from './services/application-dashboard.service';
import {ApplicationDTO} from './models/application.model';

@Component({
  selector: 'osm-application-dashboard',
  templateUrl: './application-dashboard.component.html',
  styleUrls: ['./application-dashboard.component.scss']
})
export class ApplicationDashboardComponent implements OnDestroy {
  applications$: Observable<ApplicationDTO[]>;
  selectedApplication: ApplicationDTO;
  destroyed$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private dashboardService: ApplicationDashboardService
  ) {
    this.applications$ = dashboardService.activeOrRecentlyMeasured$.pipe(
      map((applications: ApplicationDTO[]) => this.sortApplicationsByName(applications))
    );

    combineLatest(this.route.paramMap, this.applications$)
      .pipe(takeUntil(this.destroyed$))
      .subscribe(([navParams, applications]) => this.handleNavigation(navParams.get('applicationId'), applications));
  }

  private handleNavigation(applicationId: string, applications: ApplicationDTO[]) {
    if (!applicationId) {
      this.updateApplication(applications[0]);
      return;
    }
    this.selectedApplication = this.findApplicationById(applications, applicationId);
    if (this.selectedApplication) {
      this.dashboardService.updateApplicationData(this.selectedApplication);
    }
  }

  ngOnDestroy() {
    this.destroyed$.next(null);
    this.destroyed$.complete();
  }

  updateApplication(application: ApplicationDTO) {
    this.router.navigate(['/applicationDashboard', application.id]);
  }

  private findApplicationById(applications: ApplicationDTO[], applicationId: string) {
    return applications.find(application => application.id == Number(applicationId));
  }

  private sortApplicationsByName(applications: ApplicationDTO[]) {
    return applications.sort((a, b) => a.name.localeCompare(b.name, [], {sensitivity: 'base'}));
  }
}
