import {NgModule} from '@angular/core';
import {ApplicationDashboardComponent} from './application-dashboard.component';
import {ApplicationSelectComponent} from './component/application-select/application-select.component';
import {SharedModule} from "../shared/shared.module";
import {PageListComponent} from './component/page-list/page-list.component';
import {ApplicationDashboardService} from "./service/application-dashboard.service";
import {PageComponent} from './component/page/page.component';
import {ApplicationCsiComponent} from './component/application-csi/application-csi.component';
import {CsiService} from "./service/csi.service";
import {ApplicationDashboardRoutingModule} from "./application-dashboard-routing/application-dashboard-routing.module";
import {ApplicationDashboardEntryComponent} from './application-dashboard-routing/application-dashboard-entry.component'
import {JobGroupService} from "./service/job-group.service";

@NgModule({
  imports: [
    SharedModule, ApplicationDashboardRoutingModule
  ],
  declarations: [ApplicationDashboardComponent, ApplicationSelectComponent, PageListComponent, PageComponent, ApplicationCsiComponent, ApplicationDashboardEntryComponent],
  providers: [
    {
      provide: 'components',
      useValue: [ApplicationDashboardEntryComponent],
      multi: true
    },
    JobGroupService,
    ApplicationDashboardService,
    CsiService
  ],
  entryComponents: [ApplicationDashboardEntryComponent]
})
export class ApplicationDashboardModule {
}
