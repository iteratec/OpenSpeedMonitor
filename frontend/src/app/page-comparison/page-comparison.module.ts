import {NgModule} from '@angular/core';
import {PageComparisonComponent} from './page-comparison.component';
import {PageComparisonRowComponent} from "./page-comparison-row/page-comparison-row.component";
import {SharedModule} from "../shared/shared.module";
import {JobGroupService} from "./service/job-group.service";

@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [PageComparisonComponent, PageComparisonRowComponent],
  providers: [
    {provide: 'components', useValue: [PageComparisonComponent], multi: true}, JobGroupService
  ],
  entryComponents: [PageComparisonComponent]
})
export class PageComparisonModule { }
