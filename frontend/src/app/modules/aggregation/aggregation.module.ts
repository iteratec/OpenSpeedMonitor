import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from "@angular/router";
import {SharedModule} from "../shared/shared.module";
import {AggregationComponent} from './aggregation.component';
import {ResultSelectionModule} from "../result-selection/result-selection.module";

const AggregationRoutes: Routes = [
  {path: '', component: AggregationComponent},
];

@NgModule({
  declarations: [AggregationComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(AggregationRoutes),
    SharedModule,
    ResultSelectionModule
  ]
})
export class AggregationModule { }
