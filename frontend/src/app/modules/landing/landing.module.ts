import {NgModule} from '@angular/core';
import {LandingComponent} from './landing.component';
import {LandingService} from "./services/landing.service";
import {RouterModule} from "@angular/router";
import {SharedModule} from "../shared.module";

@NgModule({
  imports: [
    RouterModule.forChild([{path: '', component: LandingComponent}]),
    SharedModule
  ],
  declarations: [LandingComponent],
  providers: [
    LandingService
  ],
})
export class LandingModule {
}
