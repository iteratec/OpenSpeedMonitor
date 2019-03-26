import {NgModule} from '@angular/core';
import {MeasurandSelectComponent} from "./components/measurand-select/measurand-select.component";
import {SharedModule} from "../shared/shared.module";
import {FormsModule} from "@angular/forms";

@NgModule({
  imports: [
    SharedModule,
    FormsModule
  ],
  declarations: [MeasurandSelectComponent],
  exports: [MeasurandSelectComponent]
})
export class ResultSelectionModule { }
