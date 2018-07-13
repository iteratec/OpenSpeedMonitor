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
import {ApplicationDashboardEntryComponent} from './application-dashboard.entry-component'
import {RouterModule, Routes} from "@angular/router";

const DashboardRoutes: Routes = [
  {path: '', component: ApplicationDashboardComponent},
  {path: ':applicationId', component: ApplicationDashboardComponent}
];

@NgModule({
  imports: [
    SharedModule,
    RouterModule.forChild(DashboardRoutes)
  ],
  exports: [
    RouterModule
  ],
  declarations: [
    ApplicationDashboardComponent,
    ApplicationSelectComponent,
    PageListComponent,
    PageComponent,
    ApplicationCsiComponent,
    ApplicationDashboardEntryComponent
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
