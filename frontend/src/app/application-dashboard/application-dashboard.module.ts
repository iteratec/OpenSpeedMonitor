import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ApplicationDashboardComponent} from './application-dashboard.component';

@NgModule({
  imports: [
    CommonModule
  ],
  declarations: [ApplicationDashboardComponent],
  providers: [
    {
      provide: 'components',
      useValue: [ApplicationDashboardComponent],
      multi: true
    }
  ],
  entryComponents: [ApplicationDashboardComponent]
})
export class ApplicationDashboardModule {
}
