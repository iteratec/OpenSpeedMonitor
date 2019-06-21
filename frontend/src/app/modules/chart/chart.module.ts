import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from "../shared/shared.module";
import {BarchartDataService} from "./services/barchart-data.service";

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    SharedModule
  ],
  providers: [
    BarchartDataService
  ]
})
export class ChartModule { }
