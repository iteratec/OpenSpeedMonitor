import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SetupDashboardComponent } from './setup-dashboard.component';
import { HttpClientModule } from "@angular/common/http";
import { JobGroupRestService } from "./service/rest/job-group-rest.service";
import { UrlStore } from "../common/app.url-store";
import {PageService} from "./service/rest/page.service";
import { PageComponent } from './component/page/page.component';
import { PageListComponent } from './component/page-list/page-list.component';
import { ScriptListComponent } from './component/script-list/script-list.component';
import {ScriptService} from "./service/rest/script.service";
import {ScriptComponent} from "./component/script/script.component";

@NgModule({
  imports: [
    CommonModule, HttpClientModule
  ],
  declarations: [SetupDashboardComponent, PageComponent, PageListComponent, ScriptComponent, ScriptListComponent],
  providers: [
    { provide: 'components', useValue: [SetupDashboardComponent], multi: true}, JobGroupRestService, PageService, ScriptService
  ],
  entryComponents: [SetupDashboardComponent]
})
export class SetupDashboardModule { }
