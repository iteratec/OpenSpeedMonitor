import {NgModule} from '@angular/core';
import {ApplicationDashboardComponent} from './application-dashboard.component';
import {ApplicationSelectComponent} from './component/application-select/application-select.component';
import {JobGroupService} from '../shared/service/rest/job-group.service';
import {SharedModule} from '../shared/shared.module';
import {PageListComponent} from './component/page-list/page-list.component';
import {ApplicationDashboardService} from './service/application-dashboard.service';
import {PageComponent} from './component/page/page.component';
import {ApplicationCsiComponent} from './component/application-csi/application-csi.component';
import {CsiService} from './service/csi.service';
import {RouterModule, Routes} from '@angular/router';
import {ApplicationDashboardEntryComponent} from './application-dashboard.entry-component';

const DashboardRoutes: Routes = [
  {path: '', component: ApplicationDashboardComponent},
  {path: ':jobGroupId', component: ApplicationDashboardComponent}
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
