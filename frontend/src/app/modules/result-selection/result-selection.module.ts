import { NgModule } from '@angular/core';
import {MeasurandSelectComponent} from "./components/measurand-select/measurand-select.component";
import {SharedModule} from "../shared/shared.module";
import { MeasurandGroupComponent } from './components/measurand-select/measurand-group/measurand-group.component';

@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [MeasurandSelectComponent, MeasurandGroupComponent],
  exports: [MeasurandSelectComponent]
})
export class ResultSelectionModule { }
