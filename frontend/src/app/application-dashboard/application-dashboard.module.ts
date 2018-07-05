import {NgModule} from '@angular/core';
import {ApplicationDashboardComponent} from './application-dashboard.component';
import {ApplicationSelectComponent} from './component/application-select/application-select.component';
import {JobGroupService} from "../shared/service/rest/job-group.service";
import {SharedModule} from "../shared/shared.module";
import {PageListComponent} from './component/page-list/page-list.component';
import {ApplicationDashboardService} from "./service/application-dashboard.service";
import { PageComponent } from './component/page/page.component';

@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [ApplicationDashboardComponent, ApplicationSelectComponent, PageListComponent, PageComponent],
  providers: [
    {
      provide: 'components',
      useValue: [ApplicationDashboardComponent],
      multi: true
    },
    JobGroupService,
    ApplicationDashboardService
  ],
  entryComponents: [ApplicationDashboardComponent]
})
export class ApplicationDashboardModule {
}
