import {NgModule} from '@angular/core';
import {LandingComponent} from './landing.component';
import {SharedModule} from "../shared/shared.module";
import {LandingService} from "./services/landing.service";
import {RouterModule} from "@angular/router";

@NgModule({
  imports: [
    SharedModule,
    RouterModule.forChild([{path: '', component: LandingComponent}])
  ],
  declarations: [LandingComponent],
  providers: [
    LandingService
  ],
})
export class LandingModule {
}
