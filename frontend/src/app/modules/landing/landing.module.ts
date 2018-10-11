import {NgModule} from '@angular/core';
import {LandingComponent} from './landing.component';
import {RouterModule} from "@angular/router";
import {SharedModule} from "../shared/shared.module";
import {ContinueSetupComponent} from './components/continue-setup/continue-setup.component';

@NgModule({
  imports: [
    RouterModule.forChild([
      {path: '', component: LandingComponent},
      {path: 'index', component: LandingComponent},
      {path: 'continueSetup', component: ContinueSetupComponent}
    ]),
    SharedModule
  ],
  declarations: [LandingComponent, ContinueSetupComponent],
})
export class LandingModule {
}
