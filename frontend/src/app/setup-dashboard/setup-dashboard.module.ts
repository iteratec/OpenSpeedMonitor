import {InjectionToken, NgModule} from '@angular/core';
import { CommonModule } from '@angular/common';
import { SetupDashboardComponent } from './setup-dashboard.component';
import {HttpClientModule} from "@angular/common/http";
import {JobGroupRestService} from "./service/rest/job-group-rest.service";
import {Configuration} from "../common/app.url-store";

@NgModule({
  imports: [
    CommonModule, HttpClientModule
  ],
  declarations: [SetupDashboardComponent],
  providers: [
    { provide: 'components', useValue: [SetupDashboardComponent], multi: true}, JobGroupRestService, Configuration
  ],
  entryComponents: [SetupDashboardComponent]
})
export class SetupDashboardModule { }
