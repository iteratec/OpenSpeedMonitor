import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AspectConfigurationComponent} from './aspect-configuration.component';
import {RouterModule, Routes} from "@angular/router";
import {SharedModule} from "../shared/shared.module";
import {AspectMetricsComponent} from './components/aspect-metrics/aspect-metrics.component';
import {MetricFinderModule} from "../metric-finder/metric-finder.module";
import {EditAspectMetricsComponent} from './components/edit-aspect-metrics/edit-aspect-metrics.component';

const routes: Routes = [
  {path: '', component: AspectConfigurationComponent},
  {path: ':applicationId/:pageId', component: AspectConfigurationComponent},
  {path: 'edit/:applicationId/:pageId/:browserId/:aspectType', component: EditAspectMetricsComponent}
];

@NgModule({
  declarations: [AspectConfigurationComponent, AspectMetricsComponent, EditAspectMetricsComponent],
  imports: [
    RouterModule.forChild(routes),
    CommonModule,
    SharedModule,
    MetricFinderModule
  ],
  exports: [
    RouterModule
  ]
})
export class AspectConfigurationModule {
}
