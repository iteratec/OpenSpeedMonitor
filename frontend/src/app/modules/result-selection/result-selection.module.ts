import {NgModule} from '@angular/core';
import {ResultSelectionComponent} from './result-selection.component';
import {ResultSelectionService} from "./services/result-selection.service";
import {MeasurandSelectComponent} from "./components/measurand-select/measurand-select.component";
import {SharedModule} from "../shared/shared.module";
import {ResultSelectionTimeFrameComponent} from './components/result-selection-time-frame/result-selection-time-frame.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MeasurandsComponent} from './components/measurands/measurands.component';
import {
  OWL_DATE_TIME_FORMATS,
  OwlDateTimeModule,
  OwlNativeDateTimeModule
} from 'ng-pick-datetime';
import {ResultSelectionPageLocationConnectivityComponent} from './components/result-selection-page-location-connectivity/result-selection-page-location-connectivity.component';
import {NgSelectModule} from "@ng-select/ng-select";
import {ResultSelectionApplicationComponent} from './components/result-selection-application/result-selection-application.component';
import {ResultSelectionStore} from "./services/result-selection.store";

export const TIME_FORMAT = {
  fullPickerInput: {year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'},
  monthYearLabel: {year: 'numeric', month: 'short'},
};

@NgModule({
  imports: [
    SharedModule,
    ReactiveFormsModule,
    FormsModule,
    NgSelectModule,
    OwlDateTimeModule,
    OwlNativeDateTimeModule
  ],
  declarations: [
    ResultSelectionComponent,
    ResultSelectionTimeFrameComponent,
    ResultSelectionApplicationComponent,
    ResultSelectionPageLocationConnectivityComponent,
    MeasurandSelectComponent,
    MeasurandsComponent
  ],
  exports: [MeasurandSelectComponent],
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
    ResultSelectionService,
    ResultSelectionStore
  ],
  entryComponents: [
    ResultSelectionComponent
  ]
})
export class ResultSelectionModule { }
