
import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {MetricFinderComponent} from './metric-finder.component';
import {FilmstripComponent} from './filmstrip-component/filmstrip.component';

@NgModule({
  imports: [
    RouterModule.forChild([{path: '', component: MetricFinderComponent}]),
  ],
  declarations: [
    MetricFinderComponent,
    FilmstripComponent
  ],
  exports: [
    RouterModule
  ]
})
export class MetricFinderModule { }
