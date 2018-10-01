import { NgModule } from '@angular/core';
import { LandingComponent } from './landing.component';
import {SharedModule} from "../shared/shared.module";

@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [LandingComponent],
  providers: [
    {provide: 'components', useValue: [LandingComponent], multi: true}
  ],
  entryComponents: [
    LandingComponent
  ]
})
export class LandingModule { }
