import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimeSeriesComponent } from './time-series.component';
import {RouterModule, Routes} from "@angular/router";
import {SharedModule} from "../shared/shared.module";
import {ResultSelectionModule} from "../result-selection/result-selection.module";

const TimeSeriesRoutes: Routes = [
  {path: 'showAll', component: TimeSeriesComponent},
];

@NgModule({
  declarations: [TimeSeriesComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(TimeSeriesRoutes),
    SharedModule,
    ResultSelectionModule
  ]
})
export class TimeSeriesModule { }
