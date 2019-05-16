import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {MetricFinderService} from './services/metric-finder.service';
import {LineChartComponent} from './components/line-chart/line-chart.component';
import {FormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {MetricFinderComponent} from './metric-finder.component';
import {FilmstripComponent} from './components/filmstrip-component/filmstrip.component';
import {FilmstripService} from './services/filmstrip.service';
import {CommonModule} from '@angular/common';

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
    RouterModule.forChild([{path: '', component: MetricFinderComponent}]),
    FormsModule
  ],
  providers: [
    MetricFinderService,
    FilmstripService
  ],
})
export class MetricFinderModule {
}
