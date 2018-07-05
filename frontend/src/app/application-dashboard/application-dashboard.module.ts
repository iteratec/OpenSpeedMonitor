import {NgModule} from '@angular/core';
import {ApplicationDashboardComponent} from './application-dashboard.component';
import {ApplicationSelectComponent} from './component/application-select/application-select.component';
import {JobGroupService} from "../shared/service/rest/job-group.service";
import {SharedModule} from "../shared/shared.module";
import {PageListComponent} from './component/page-list/page-list.component';
import {ApplicationDashboardService} from "./service/application-dashboard.service";
import {ApplicationCsiComponent} from './component/application-csi/application-csi.component';
import {CsiService} from "./service/csi.service";
import {ApplicationDashboardRoutingModule} from "./application-dashboard-routing/application-dashboard-routing.module";
import {ApplicationDashboardEntryComponent} from './application-dashboard-routing/application-dashboard-entry.component'
import {CsiValueComponent} from './component/csi-value/csi-value.component';

@NgModule({
  imports: [
    SharedModule, ApplicationDashboardRoutingModule
  ],
  declarations: [
    ApplicationDashboardEntryComponent,
    ApplicationDashboardComponent,
    ApplicationSelectComponent,
    ApplicationCsiComponent,
    PageListComponent,
    CsiValueComponent
  ],
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
