import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {ApplicationDashboardComponent} from "./application-dashboard.component";

const DashboardRoutes: Routes = [
  // {
  //   path: 'application-dashboard',
  //   component: ApplicationDashboardComponent,
  //     children: [{path: 'application-dashboard/:jobGroupName', component: ApplicationDashboardComponent}]
  // },
  {path: 'application-dashboard/:name', component: ApplicationDashboardComponent},
];


//taken from angular.io
//Only call RouterModule.forRoot in the root AppRoutingModule (or the AppModule if
//that's where you register top level application routes). In any other module, you
//must call the RouterModule.forChild method to register additional routes.
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
