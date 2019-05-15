import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {HttpClientModule} from '@angular/common/http';
import {MetricFinderComponent} from './metric-finder.component';
import {FilmstripComponent} from './components/filmstrip-component/filmstrip.component';
import {FilmstripService} from './services/filmstrip.service';
import {CommonModule} from '@angular/common';
import {FormsModule} from "@angular/forms";

@NgModule({
  imports: [
    RouterModule.forChild([{path: '', component: MetricFinderComponent}]),
    HttpClientModule,
    CommonModule,
    FormsModule,
  ],
  declarations: [
    MetricFinderComponent,
    FilmstripComponent
  ],
  providers: [
    FilmstripService
  ],
  exports: [
    RouterModule,
  ]
})
export class MetricFinderModule {
}
