import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AspectConfigurationComponent} from './aspect-configuration.component';
import {RouterModule, Routes} from "@angular/router";
import {SharedModule} from "../shared/shared.module";
import {AspectMetricsComponent} from './components/aspect-metrics/aspect-metrics.component';

const routes: Routes = [
  {path: '', component: AspectConfigurationComponent},
  {path: ':applicationId/:pageId', component: AspectConfigurationComponent}
];

@NgModule({
  declarations: [AspectConfigurationComponent, AspectMetricsComponent],
  imports: [
    RouterModule.forChild(routes),
    CommonModule,
    SharedModule
  ],
  exports: [
    RouterModule
  ]
})
export class AspectConfigurationModule {
}
