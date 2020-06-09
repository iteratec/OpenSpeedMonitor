import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {JobResultComponent} from './job-result.component';
import {RouterModule, Routes} from '@angular/router';
import {SharedModule} from '../shared/shared.module';

const JobResultRoutes: Routes = [
  {path: 'list', component: JobResultComponent},
];

@NgModule({
  declarations: [JobResultComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(JobResultRoutes),
    SharedModule
  ]
})
export class JobResultModule {
}
