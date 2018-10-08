import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TranslateModule} from "@ngx-translate/core";
import {CsiValueBaseComponent} from "./components/csi-value/csi-value-base.component";
import {CsiValueBigComponent} from "./components/csi-value/csi-value-big/csi-value-big.component";
import {CsiValueMediumComponent} from "./components/csi-value/csi-value-medium/csi-value-medium.component";
import {CsiValueSmallComponent} from "./components/csi-value/csi-value-small/csi-value-small.component";

@NgModule({
  imports: [
    CommonModule,
    TranslateModule.forChild()
  ],
  declarations: [
    CsiValueBaseComponent,
    CsiValueBigComponent,
    CsiValueMediumComponent,
    CsiValueSmallComponent
  ],
  exports: [
    CommonModule,
    TranslateModule,
    CsiValueBigComponent,
    CsiValueMediumComponent,
    CsiValueSmallComponent
  ]
})
export class SharedModule {
}
