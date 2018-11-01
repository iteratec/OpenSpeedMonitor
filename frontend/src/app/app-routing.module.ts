import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {NotFoundComponent} from './not-found.component';


const appRoutes: Routes = [
  {
    path: 'applicationDashboard',
    loadChildren: './modules/application-dashboard/application-dashboard.module#ApplicationDashboardModule'
  },
  {
    path: 'queueDashboard',
    loadChildren: './modules/queue-dashboard/queue-dashboard.module#QueueDashboardModule'
  },
  {
    path: 'landing',
    loadChildren: './modules/landing/landing.module#LandingModule'
  },
  {
    path: '',
    loadChildren: './modules/landing/landing.module#LandingModule',
    pathMatch: 'full'
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
