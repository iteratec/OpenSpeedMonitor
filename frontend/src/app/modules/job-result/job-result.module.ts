import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {JobResultComponent} from './job-result.component';
import {RouterModule, Routes} from '@angular/router';

const JobResultRoutes: Routes = [
  {path: 'list', component: JobResultComponent},
];

@NgModule({
  declarations: [JobResultComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(JobResultRoutes)
  ]
})
export class JobResultModule {
}
