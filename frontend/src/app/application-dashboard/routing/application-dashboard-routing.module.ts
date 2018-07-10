import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {ApplicationDashboardComponent} from "../application-dashboard.component";

const DashboardRoutes: Routes = [
  {path: '', component: ApplicationDashboardComponent},
  {path: ':jobGroupId', component: ApplicationDashboardComponent}

];

@NgModule({
  imports: [
    RouterModule.forChild(DashboardRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class ApplicationDashboardRoutingModule {
}
