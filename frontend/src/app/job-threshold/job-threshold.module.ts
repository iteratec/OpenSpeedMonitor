import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {JobThresholdComponent} from './job-threshold.component';
import {HttpClientModule} from "@angular/common/http";
import {ThresholdRestService} from "../job-threshold/service/rest/threshold-rest.service";
import {FormsModule} from '@angular/forms';
import { MeasuredEventComponent } from './component/measured-event/measured-event.component';

@NgModule({
  imports: [
    CommonModule, HttpClientModule, FormsModule
  ],
  declarations: [JobThresholdComponent, MeasuredEventComponent],
  providers: [
    { provide: 'components', useValue: [JobThresholdComponent], multi: true}, ThresholdRestService
  ],
  entryComponents: [JobThresholdComponent]
})
export class ThresholdModule { }
