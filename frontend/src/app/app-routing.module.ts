import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {NotFoundComponent} from './not-found.component';


const appRoutes: Routes = [{
  path: 'applicationDashboard',
  loadChildren: './application-dashboard/application-dashboard.module#ApplicationDashboardModule',
},
  {
    path: 'queueDashboard',
    loadChildren: './queue-dashboard/queue-dashboard.module#QueueDashboardModule'
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
