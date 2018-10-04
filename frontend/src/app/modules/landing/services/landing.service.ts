import {Injectable} from '@angular/core';
import {ApplicationList, ApplicationWithCsi} from "../models/application-list.model";
import {BehaviorSubject} from "rxjs/internal/BehaviorSubject";
import {ApplicationService} from "../../../services/application.service";
import {map} from "rxjs/operators";

@Injectable()
export class LandingService {

  applicationList$ = new BehaviorSubject<ApplicationList>({isLoading: true, applications: []});

  constructor(private applicationService: ApplicationService) {
    this.applicationService.applications$.pipe(
      map(applications => ({
        isLoading: false,
        applications: applications.map(application => new ApplicationWithCsi(application))
      }))
    ).subscribe(this.applicationList$);
  }
}
