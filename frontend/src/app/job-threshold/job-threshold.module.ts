import {NgModule} from "@angular/core";
import {JobThresholdComponent} from "./job-threshold.component";
import {ThresholdRestService} from "./services/threshold-rest.service";
import {MeasurandService} from "./services/measurand.service";
import {MeasuredEventService} from "./services/measured-event.service";
import {MeasuredEventComponent} from "./components/measured-event/measured-event.component";
import {ThresholdComponent} from "./components/threshold/threshold.component";
import {SharedModule} from "../shared/shared.module";
import { ThresholdRowComponent } from './components/threshold-row/threshold-row.component';

@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [JobThresholdComponent, MeasuredEventComponent, ThresholdComponent, ThresholdRowComponent],
  providers: [
    { provide: 'components', useValue: [JobThresholdComponent], multi: true}, ThresholdRestService, MeasurandService, MeasuredEventService
  ],
  entryComponents: [JobThresholdComponent]
})
export class ThresholdModule { }