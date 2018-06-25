import {NgModule} from '@angular/core';
import {ApplicationDashboardComponent} from './application-dashboard.component';
import {ApplicationSelectComponent} from './component/application-select/application-select.component';
import {JobGroupService} from "../shared/service/rest/job-group.service";
import {SharedModule} from "../shared/shared.module";
import {ApplicationCsiComponent} from './component/application-csi/application-csi.component';
import {CsiService} from "./service/rest/csi.service";

@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [ApplicationDashboardComponent, ApplicationSelectComponent, ApplicationCsiComponent],
  providers: [
    {
      provide: 'components',
      useValue: [ApplicationDashboardComponent],
      multi: true
    }, JobGroupService, CsiService
  ],
  entryComponents: [ApplicationDashboardComponent]
})
export class ApplicationDashboardModule {
}
