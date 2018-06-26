import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ApplicationDashboardComponent} from './application-dashboard.component';
import {ApplicationSelectComponent} from './component/application-select/application-select.component';
import {JobGroupService} from "../setup-dashboard/service/rest/job-group.service";
import {HttpClientModule} from "@angular/common/http";
import {FormsModule} from "@angular/forms";
import {ApplicationDashboardRoutingModule} from "./application-dashboard-routing.module"

@NgModule({
  imports: [
    CommonModule, HttpClientModule, FormsModule, ApplicationDashboardRoutingModule
  ],
  declarations: [ApplicationDashboardComponent, ApplicationSelectComponent],
  providers: [
    {
      provide: 'components',
      useValue: [ApplicationDashboardComponent],
      multi: true
    }, JobGroupService
  ],
  entryComponents: [ApplicationDashboardComponent]
})
export class ApplicationDashboardModule {
}
