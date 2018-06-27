import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ApplicationDashboardComponent} from './application-dashboard-entry/application-dashboard.component';
import {ApplicationSelectComponent} from './component/application-select/application-select.component';
import {JobGroupService} from "../setup-dashboard/service/rest/job-group.service";
import {HttpClientModule} from "@angular/common/http";
import {FormsModule} from "@angular/forms";
import {ApplicationDashboardRoutingModule} from "./application-dashboard-routing.module";
import {ApplicationDashboardEntryComponent} from './application-dashboard-entry.component'

@NgModule({
  imports: [
    CommonModule, HttpClientModule, FormsModule, ApplicationDashboardRoutingModule
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
