import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PageComparisonComponent} from './pageComparison.component';
import {HttpClientModule} from "@angular/common/http";
import {PageComparisonRowComponent} from "./pageComparisonRow/pageComparisonRow.component";
import {ResultSelectionService} from "./service/resultSelection.service";

@NgModule({
  imports: [
    CommonModule, HttpClientModule
  ],
  declarations: [PageComparisonComponent, PageComparisonRowComponent],
  providers: [
    { provide: 'components', useValue: [PageComparisonComponent], multi: true}, ResultSelectionService
  ],
  entryComponents: [PageComparisonComponent]
})
export class PageComparisonModule { }
