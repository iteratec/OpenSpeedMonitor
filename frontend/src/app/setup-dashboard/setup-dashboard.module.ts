import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SetupDashboardComponent} from './setup-dashboard.component';
import {HttpClientModule} from "@angular/common/http";
import {PageService} from "./service/rest/page.service";
import {PageComponent} from './component/page/page.component';
import {PageListComponent} from './component/page-list/page-list.component';
import {ScriptListComponent} from './component/script-list/script-list.component';
import {ScriptService} from "./service/rest/script.service";
import {ScriptComponent} from "./component/script/script.component";
import {JobGroupService} from "./service/rest/job-group.service";
import {JobGroupComponent} from "./component/job-group/job-group/job-group.component";
import {JobGroupListComponent} from "./component/job-group-list/job-group-list/job-group-list.component";

@NgModule({
  imports: [
    CommonModule, HttpClientModule
  ],
  declarations:
    [SetupDashboardComponent, JobGroupComponent, JobGroupListComponent, PageComponent, PageListComponent, ScriptComponent, ScriptListComponent],
  providers: [
    {
      provide: 'components',
      useValue: [SetupDashboardComponent],
      multi: true
    }, JobGroupService, PageService, ScriptService
  ],
  entryComponents: [SetupDashboardComponent]
})
export class SetupDashboardModule { }
