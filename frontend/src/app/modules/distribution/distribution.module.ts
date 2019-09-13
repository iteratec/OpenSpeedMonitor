import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DistributionComponent} from './distribution.component';
import {RouterModule, Routes} from "@angular/router";
import {ResultSelectionModule} from "../result-selection/result-selection.module";
import {SharedModule} from "../shared/shared.module";
import {ViolinchartDataService} from "./services/violinchart-data.service";

const DistributionRoutes: Routes = [
  {path: 'show', component: DistributionComponent},
];

@NgModule({
  declarations: [DistributionComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(DistributionRoutes),
    SharedModule,
    ResultSelectionModule,
  ],
  providers: [
    ViolinchartDataService
  ]
})
export class DistributionModule { }
