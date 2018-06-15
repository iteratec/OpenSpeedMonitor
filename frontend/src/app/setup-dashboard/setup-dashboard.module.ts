import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SetupDashboardComponent} from './setup-dashboard.component';
import {HttpClientModule} from "@angular/common/http";
import {JobGroupRestService} from "./service/rest/job-group-rest.service";
import {TranslationModule} from "../translation/translation.module";

@NgModule({
  imports: [
    CommonModule,
    HttpClientModule,
    TranslationModule
  ],
  declarations: [SetupDashboardComponent],
  providers: [
    {provide: 'components', useValue: [SetupDashboardComponent], multi: true}, JobGroupRestService
  ],
  entryComponents: [SetupDashboardComponent]
})
export class SetupDashboardModule {
}
