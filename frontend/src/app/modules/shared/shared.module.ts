import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TranslateModule} from "@ngx-translate/core";
import {CsiValueBaseComponent} from "./components/csi-value/csi-value-base.component";
import {CsiValueBigComponent} from "./components/csi-value/csi-value-big/csi-value-big.component";
import {CsiValueMediumComponent} from "./components/csi-value/csi-value-medium/csi-value-medium.component";
import {CsiValueSmallComponent} from "./components/csi-value/csi-value-small/csi-value-small.component";
import {EmptyStateComponent} from './components/empty-state/empty-state.component';
import {NgxSmartModalModule} from "ngx-smart-modal";

@NgModule({
  imports: [
    CommonModule,
    TranslateModule.forChild(),
    NgxSmartModalModule.forChild()
  ],
  declarations: [
    CsiValueBaseComponent,
    CsiValueBigComponent,
    CsiValueMediumComponent,
    CsiValueSmallComponent,
    EmptyStateComponent
  ],
  exports: [
    CommonModule,
    TranslateModule,
    CsiValueBigComponent,
    CsiValueMediumComponent,
    CsiValueSmallComponent,
    EmptyStateComponent,
    NgxSmartModalModule
  ]
})
export class SharedModule {
}
