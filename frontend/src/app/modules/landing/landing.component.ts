import {Component} from '@angular/core';
import { filter, map } from 'rxjs/operators';
import {ApplicationService} from "../../services/application.service";
import {combineLatest, Observable} from "rxjs";
import {ApplicationWithCsi} from "./models/application-with-csi.model";
import {ResponseWithLoadingState} from "../../models/response-with-loading-state.model";
import { FailingJob } from './models/failing-jobs.model';

@Component({
  selector: 'osm-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent {
  objectKeys = Object.keys;
  showApplicationEmptyState$: Observable<boolean>;
  hasData$: Observable<boolean>;
  applications$: Observable<ApplicationWithCsi[]>;
  failingJobs$: Observable<{[application: string]: FailingJob[]}>;

  constructor(private applicationService: ApplicationService) {
    this.hasData$ = this.applicationService.applications$.pipe(map(response => this.dataHasLoaded(response)));
    this.showApplicationEmptyState$ = this.applicationService.applications$.pipe(
      map(response => this.dataHasLoaded(response) && response.data.length < 1),
    );
    this.applications$ = combineLatest(this.applicationService.applications$, this.applicationService.applicationCsiById$).pipe(
      filter(([applications]) => !applications.isLoading && !!applications.data),
      map(([applications, csiById]) => applications.data.map(app => new ApplicationWithCsi(app, csiById[app.id], csiById.isLoading)))
    );
    this.applicationService.loadApplications();
    this.applicationService.loadRecentCsiForApplications();
    this.failingJobs$ = this.applicationService.getFailingJobs().pipe(
      map(failingJobs => {
        if (failingJobs) {
          return failingJobs.reduce((failingJobsByApplication, currentValue) => {
            if (!failingJobsByApplication[currentValue.application]) {
              failingJobsByApplication[currentValue.application] = [];
            }
            failingJobsByApplication[currentValue.application].push(currentValue);
            return failingJobsByApplication;
          }, {});
        }
      }
      )
    );
  }

  private dataHasLoaded(response: ResponseWithLoadingState<object>): boolean {
    return !response.isLoading && !!response.data;
  }

}
