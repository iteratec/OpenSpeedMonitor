import {InjectionToken, NgModule} from '@angular/core';
import { CommonModule } from '@angular/common';
import { SetupDashboardComponent } from './setup-dashboard.component';

@NgModule({
  imports: [
    CommonModule
  ],
  declarations: [SetupDashboardComponent],
  providers: [
    { provide: 'components', useValue: [SetupDashboardComponent], multi: true }
  ],
  entryComponents: [SetupDashboardComponent]
})
export class SetupDashboardModule { }
