import {NgModule} from '@angular/core';
import {SetupDashboardComponent} from './setup-dashboard.component';
import {PageService} from "./service/rest/page.service";
import {PageComponent} from './component/page/page.component';
import {PageListComponent} from './component/page-list/page-list.component';
import {ScriptListComponent} from './component/script-list/script-list.component';
import {ScriptService} from "./service/rest/script.service";
import {ScriptComponent} from "./component/script/script.component";
import {JobGroupComponent} from "./component/job-group/job-group/job-group.component";
import {JobGroupListComponent} from "./component/job-group-list/job-group-list/job-group-list.component";
import {SharedModule} from "../shared/shared.module";
import {TranslationModule} from "../translation/translation.module";
import {HttpClientModule} from "@angular/common/http";
import {JobGroupService} from "./service/job-group.service";

@NgModule({
  imports: [
    SharedModule,
    HttpClientModule,
    TranslationModule
  ],
  declarations:
    [SetupDashboardComponent, JobGroupComponent, JobGroupListComponent, PageComponent, PageListComponent, ScriptComponent, ScriptListComponent],
  providers: [
    {
      provide: 'components',
      useValue: [SetupDashboardComponent],
      multi: true
    }, PageService, ScriptService, JobGroupService
  ],
  entryComponents: [SetupDashboardComponent]
})
export class SetupDashboardModule {
}
