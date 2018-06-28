import {NgModule} from '@angular/core';
import {ApplicationDashboardComponent} from './application-dashboard-entry/application-dashboard.component';
import {ApplicationSelectComponent} from './component/application-select/application-select.component';
import {JobGroupService} from "../shared/service/rest/job-group.service";
import {SharedModule} from "../shared/shared.module";
import {ApplicationDashboardRoutingModule} from "./application-dashboard-routing.module";
import {ApplicationDashboardEntryComponent} from './application-dashboard-entry.component'

@NgModule({
  imports: [
    SharedModule, ApplicationDashboardRoutingModule
  ],
  declarations: [ApplicationDashboardEntryComponent, ApplicationSelectComponent, ApplicationDashboardComponent],
  providers: [
    {
      provide: 'components',
      useValue: [ApplicationDashboardEntryComponent],
      multi: true
    },
    JobGroupService,
  ],
  entryComponents: [ApplicationDashboardEntryComponent]
})
export class ApplicationDashboardModule {
}
