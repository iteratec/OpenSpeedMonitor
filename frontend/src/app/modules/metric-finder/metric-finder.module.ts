import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {MetricFinderService} from './services/metric-finder.service';
import {LineChartComponent} from './components/line-chart/line-chart.component';
import {FormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {MetricFinderComponent} from './metric-finder.component';
import {FilmstripComponent} from './components/filmstrip/filmstrip.component';
import {FilmstripService} from './services/filmstrip.service';
import {CommonModule} from '@angular/common';
import {TranslateModule} from '@ngx-translate/core';
import {ComparableFilmstripsComponent} from './components/comparable-filmstrips/comparable-filmstrips.component';

@NgModule({
  declarations: [
    MetricFinderComponent,
    LineChartComponent,
    FilmstripComponent,
    ComparableFilmstripsComponent
  ],
  imports: [
    RouterModule.forChild([{path: '', component: MetricFinderComponent}]),
    HttpClientModule,
    CommonModule,
    FormsModule,
    TranslateModule.forChild()
  ],
  providers: [
    MetricFinderService,
    FilmstripService
  ],
  exports: [
    MetricFinderComponent
  ]
})
export class MetricFinderModule {
}
