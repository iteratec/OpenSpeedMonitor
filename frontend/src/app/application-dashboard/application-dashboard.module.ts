import {NgModule} from '@angular/core';
import {ApplicationDashboardComponent} from './application-dashboard.component';
import {ApplicationSelectComponent} from './component/application-select/application-select.component';
import {JobGroupService} from "../shared/service/rest/job-group.service";
import {SharedModule} from "../shared/shared.module";
import { CsiValueComponent } from './component/csi-value/csi-value.component';

@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [ApplicationDashboardComponent, ApplicationSelectComponent, CsiValueComponent],
  providers: [
    {
      provide: 'components',
      useValue: [ApplicationDashboardComponent],
      multi: true
    }, JobGroupService
  ],
  entryComponents: [ApplicationDashboardComponent]
})
export class ApplicationDashboardModule {
}
