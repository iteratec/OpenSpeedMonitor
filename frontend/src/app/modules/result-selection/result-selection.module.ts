import { NgModule } from '@angular/core';
import { ResultSelectionComponent } from './result-selection.component';
import {ResultSelectionService} from "./services/result-selection.service";
import {SharedModule} from "../shared/shared.module";
import { ResultSelectionTimeFrameComponent } from './components/result-selection-time-frame/result-selection-time-frame.component';
import { ResultSelectionApplicationComponent } from './components/result-selection-application/result-selection-application.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {
  OWL_DATE_TIME_FORMATS,
  OwlDateTimeModule,
  OwlNativeDateTimeModule
} from 'ng-pick-datetime';

export const TIME_FORMAT = {
  fullPickerInput: {year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'},
  monthYearLabel: {year: 'numeric', month: 'short'},
};

@NgModule({
  imports: [
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    OwlDateTimeModule,
    OwlNativeDateTimeModule
  ],
  declarations: [
    ResultSelectionComponent,
    ResultSelectionTimeFrameComponent,
    ResultSelectionApplicationComponent,
  ],
  providers: [
    {
      provide: 'components',
      useValue: [ResultSelectionComponent],
      multi: true
    },
    {
      provide: OWL_DATE_TIME_FORMATS,
      useValue: TIME_FORMAT
    },
    ResultSelectionService
  ],
  entryComponents: [
    ResultSelectionComponent
  ]
})
export class ResultSelectionModule { }
