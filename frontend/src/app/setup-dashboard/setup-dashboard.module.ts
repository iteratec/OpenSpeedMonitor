import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SetupDashboardComponent } from './setup-dashboard.component';
import { HttpClientModule } from "@angular/common/http";
import { JobGroupRestService } from "./service/rest/job-group-rest.service";
import { UrlStore } from "../common/app.url-store";
import {PageService} from "./service/rest/page.service";
import { PageComponent } from './component/page/page.component';
import { PageListComponent } from './component/page-list/page-list.component';

@NgModule({
  imports: [
    CommonModule, HttpClientModule
  ],
  declarations: [SetupDashboardComponent, PageComponent, PageListComponent],
  providers: [
    { provide: 'components', useValue: [SetupDashboardComponent], multi: true}, JobGroupRestService, PageService
  ],
  entryComponents: [SetupDashboardComponent]
})
export class SetupDashboardModule { }
