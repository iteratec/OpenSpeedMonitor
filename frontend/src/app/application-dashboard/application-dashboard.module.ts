import {NgModule} from '@angular/core';
import {ApplicationDashboardComponent} from './application-dashboard.component';
import {ApplicationSelectComponent} from './components/application-select/application-select.component';
import {JobGroupService} from "../shared/services/rest/job-group.service";
import {SharedModule} from "../shared/shared.module";
import {PageListComponent} from './components/page-list/page-list.component';
import {ApplicationDashboardService} from "./services/application-dashboard.service";
import {PageComponent} from './components/page/page.component';
import {ApplicationCsiComponent} from './components/application-csi/application-csi.component';
import {CsiService} from "./services/csi.service";
import {ApplicationDashboardRoutingModule} from "./routing/application-dashboard-routing.module";
import {ApplicationDashboardEntryComponent} from './routing/application-dashboard-entry.component'

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
