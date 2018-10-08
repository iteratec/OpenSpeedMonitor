import {NgModule} from '@angular/core';
import {LandingComponent} from './landing.component';
import {RouterModule} from "@angular/router";
import {SharedModule} from "../shared/shared.module";

@NgModule({
  imports: [
    RouterModule.forChild([{path: '', component: LandingComponent}]),
    SharedModule
  ],
  declarations: [LandingComponent],
})
export class LandingModule {
}
