import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {HttpClientModule} from '@angular/common/http';
import {MetricFinderComponent} from './metric-finder.component';
import {FilmstripComponent} from './components/filmstrip-component/filmstrip.component';
import {FilmstripService} from './services/filmstrip.service';
import {CommonModule} from '@angular/common';
import {LineChartComponent} from './components/line-chart/line-chart.component';
import {MetricFinderService} from './services/metric-finder.service';

@NgModule({
  declarations: [
    MetricFinderComponent,
    LineChartComponent,
    FilmstripComponent
  ],
  imports: [
    RouterModule.forChild([{path: '', component: MetricFinderComponent}]),
    HttpClientModule,
    CommonModule,
  ],
  providers: [
    MetricFinderService,
    FilmstripService
  ],
})
export class MetricFinderModule {
}
