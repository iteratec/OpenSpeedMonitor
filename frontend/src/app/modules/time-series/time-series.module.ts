import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimeSeriesComponent } from './time-series.component';
import {RouterModule, Routes} from "@angular/router";
import {SharedModule} from "../shared/shared.module";
import {ResultSelectionModule} from "../result-selection/result-selection.module";
import {TimeSeriesLineChartComponent} from './components/time-series-line-chart/time-series-line-chart.component';
import {LineChartService} from './services/line-chart.service';
import {LinechartDataService} from './services/linechart-data.service';

const TimeSeriesRoutes: Routes = [
  {path: 'showAll', component: TimeSeriesComponent},
];

@NgModule({
  declarations: [
    TimeSeriesComponent,
    TimeSeriesLineChartComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(TimeSeriesRoutes),
    SharedModule,
    ResultSelectionModule
  ],
  providers: [
    LineChartService,
    LinechartDataService
  ],
  exports: [
    TimeSeriesComponent
  ]
})
export class TimeSeriesModule { }
