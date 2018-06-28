import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {NotFoundComponent} from "./not-found.component";


const appRoutes: Routes = [{
  path: 'application-dashboard',
  loadChildren: './application-dashboard/application-dashboard.module#ApplicationDashboardModule'
},
  {path: '**', component: NotFoundComponent}];

@NgModule({
  imports: [
    //use  RouterModule.forRoot(appRoutes,{enableTracing: true}) for debugging
    RouterModule.forRoot(appRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class AppRoutingModule {
}
