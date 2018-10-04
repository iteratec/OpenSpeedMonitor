import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {NotFoundComponent} from './not-found.component';


const appRoutes: Routes = [
  {
    path: 'applicationDashboard',
    loadChildren: './modules/application-dashboard/application-dashboard.module#ApplicationDashboardModule'
  },
  {
    path: '',
    loadChildren: './modules/landing/landing.module#LandingModule'
  },
  {path: '**', component: NotFoundComponent}];

@NgModule({
  imports: [
    // RouterModule.forRoot(appRoutes, {enableTracing: true})
    RouterModule.forRoot(appRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class AppRoutingModule {
}
