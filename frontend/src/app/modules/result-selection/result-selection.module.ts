import { NgModule } from '@angular/core';
import { ResultSelectionComponent } from './result-selection.component';
import {ResultSelectionService} from "./services/result-selection.service";
import {SharedModule} from "../shared/shared.module";
import { ResultSelectionTimeFrameComponent } from './components/result-selection-time-frame/result-selection-time-frame.component';
import { ResultSelectionApplicationComponent } from './components/result-selection-application/result-selection-application.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import { OwlDateTimeModule, OwlNativeDateTimeModule } from 'ng-pick-datetime';

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
    ResultSelectionService
  ],
  entryComponents: [
    ResultSelectionComponent
  ]
})
export class ResultSelectionModule { }
