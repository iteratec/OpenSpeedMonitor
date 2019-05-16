import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MetricFinderComponent } from './metric-finder.component';
import {RouterModule} from '@angular/router';
import {MetricFinderService} from './services/metric-finder.service';
import { LineChartComponent } from './components/line-chart/line-chart.component';

@NgModule({
  declarations: [MetricFinderComponent, LineChartComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([{path: '', component: MetricFinderComponent}])
  ],
  providers: [
    MetricFinderService
  ]
})
export class MetricFinderModule { }
