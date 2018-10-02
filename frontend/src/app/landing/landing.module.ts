import { NgModule } from '@angular/core';
import { LandingComponent } from './landing.component';
import {SharedModule} from "../shared/shared.module";
import {LandingService} from "./services/landing.service";

@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [LandingComponent],
  providers: [
    {provide: 'components', useValue: [LandingComponent], multi: true},
    LandingService
  ],
  entryComponents: [
    LandingComponent
  ]
})
export class LandingModule { }
