import { NgModule } from '@angular/core';
import {MeasurandSelectComponent} from "./components/measurand-select/measurand-select.component";
import {SharedModule} from "../shared/shared.module";

@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [MeasurandSelectComponent],
  exports: [MeasurandSelectComponent]
})
export class ResultSelectionModule { }
