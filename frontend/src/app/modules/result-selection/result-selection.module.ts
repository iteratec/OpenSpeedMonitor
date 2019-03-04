import { NgModule } from '@angular/core';
import { ResultSelectionComponent } from './result-selection.component';

@NgModule({
  imports: [
  ],
  declarations: [
    ResultSelectionComponent
  ],
  providers: [
    {
      provide: 'components',
      useValue: [ResultSelectionComponent],
      multi: true
    }
  ],
  entryComponents: [
    ResultSelectionComponent
  ]
})
export class ResultSelectionModule { }
