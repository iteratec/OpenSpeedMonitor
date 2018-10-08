import {Component} from '@angular/core';
import {map, startWith, take} from "rxjs/operators";
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
  showApplicationLoading$: Observable<boolean>;
  applications$: Observable<ApplicationWithCsi[]>;

  constructor(private applicationService: ApplicationService) {
    this.showApplicationLoading$ = this.applicationService.applications$.pipe(
      take(1),
      map(_ => false),
      startWith(true)
    );
    this.showApplicationEmptyState$ = this.applicationService.applications$.pipe(
      map(applications => !applications.length),
      startWith(false)
    );
    this.applications$ = combineLatest(this.applicationService.applications$, this.applicationService.applicationCsiById$).pipe(
      map(([applications, csiById]) => applications.map(app => new ApplicationWithCsi(app, csiById[app.id])))
    )
  }

}
