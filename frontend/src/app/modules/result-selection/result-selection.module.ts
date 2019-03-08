import { NgModule } from '@angular/core';
import { ResultSelectionComponent } from './result-selection.component';
import {ResultSelectionService} from "./services/result-selection.service";
import {SharedModule} from "../shared/shared.module";

@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [
    ResultSelectionComponent
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
