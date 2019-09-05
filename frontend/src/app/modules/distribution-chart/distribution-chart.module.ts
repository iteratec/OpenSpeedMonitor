import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DistributionChartComponent} from './distribution-chart.component';
import {RouterModule, Routes} from "@angular/router";
import {ResultSelectionModule} from "../result-selection/result-selection.module";
import {SharedModule} from "../shared/shared.module";

const DistributionChartRoutes: Routes = [
  {path: 'show', component: DistributionChartComponent},
];

@NgModule({
  declarations: [DistributionChartComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(DistributionChartRoutes),
    SharedModule,
    ResultSelectionModule
  ]
})
export class DistributionChartModule { }
