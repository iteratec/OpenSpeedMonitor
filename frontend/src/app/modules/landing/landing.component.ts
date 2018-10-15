import {Component} from '@angular/core';
import {filter, map} from "rxjs/operators";
import {ApplicationService} from "../../services/application.service";
import {combineLatest, Observable} from "rxjs";
import {ApplicationWithCsi} from "./models/application-with-csi.model";

@Component({
  selector: 'osm-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent {

  showApplicationEmptyState$: Observable<boolean>;
  hasData$: Observable<boolean>;
  applications$: Observable<ApplicationWithCsi[]>;

  constructor(private applicationService: ApplicationService) {
    this.hasData$ = this.applicationService.applications$.pipe(map(response => !response.isLoading && !!response.data));
    this.showApplicationEmptyState$ = this.applicationService.applications$.pipe(
      map(response => !response.isLoading && response.data && !response.data.length),
    );
    this.applications$ = combineLatest(this.applicationService.applications$, this.applicationService.applicationCsiById$).pipe(
      filter(([applications]) => !applications.isLoading && !!applications.data),
      map(([applications, csiById]) => applications.data.map(app => new ApplicationWithCsi(app, csiById[app.id], csiById.isLoading)))
    );
    this.applicationService.loadApplications();
    this.applicationService.loadRecentCsiForApplications();
  }

}
