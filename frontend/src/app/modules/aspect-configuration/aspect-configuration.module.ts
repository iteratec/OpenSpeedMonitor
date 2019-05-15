import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AspectConfigurationComponent} from './aspect-configuration.component';
import {RouterModule, Routes} from "@angular/router";
import {SharedModule} from "../shared/shared.module";

const routes: Routes = [
  {path: '', component: AspectConfigurationComponent},
  {path: ':pageId', component: AspectConfigurationComponent}
];

@NgModule({
  declarations: [AspectConfigurationComponent],
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
