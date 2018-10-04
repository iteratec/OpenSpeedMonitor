import {Component} from '@angular/core';
import {Observable} from "rxjs/internal/Observable";
import {Application} from "./models/application.model";
import {LandingService} from "./services/landing.service";
import {map} from "rxjs/operators";

@Component({
  selector: 'osm-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent {

  showApplicationEmptyState$: Observable<boolean>;
  showApplicationLoading$: Observable<boolean>;
  applications$: Observable<Application[]>;

  constructor(private landingService: LandingService) {
    this.showApplicationEmptyState$ = this.landingService.applicationList$
      .pipe(map(state => !state.isLoading && !state.applications.length));
    this.showApplicationLoading$ = this.landingService.applicationList$
      .pipe(map(state => state.isLoading));
    this.applications$ = this.landingService.applicationList$
      .pipe(map(state => state.applications));
  }

}
