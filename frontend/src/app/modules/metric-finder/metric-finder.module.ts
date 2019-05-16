import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MetricFinderComponent } from './metric-finder.component';
import {RouterModule} from '@angular/router';
import {MetricFinderService} from './services/metric-finder.service';
import { LineChartComponent } from './components/line-chart/line-chart.component';
import {FormsModule} from '@angular/forms';

@NgModule({
  declarations: [MetricFinderComponent, LineChartComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([{path: '', component: MetricFinderComponent}]),
    FormsModule
  ],
  providers: [
    MetricFinderService
  ]
})
export class MetricFinderModule { }
