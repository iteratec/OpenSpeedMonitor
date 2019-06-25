import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from "@angular/router";
import {SharedModule} from "../shared/shared.module";
import {AggregationComponent} from './aggregation.component';
import {ResultSelectionModule} from "../result-selection/result-selection.module";
import { AggregationChartComponent } from './components/aggregation-chart/aggregation-chart.component';

const AggregationRoutes: Routes = [
  {path: 'show', component: AggregationComponent},
];

@NgModule({
  declarations: [AggregationComponent, AggregationChartComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(AggregationRoutes),
    SharedModule,
    ResultSelectionModule
  ]
})
export class AggregationModule { }
