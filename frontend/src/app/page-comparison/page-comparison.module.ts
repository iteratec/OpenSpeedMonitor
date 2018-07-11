import {NgModule} from '@angular/core';
import {PageComparisonComponent} from './page-comparison.component';
import {PageComparisonRowComponent} from "./components/page-comparison-row/page-comparison-row.component";
import {JobGroupService} from "../shared/services/rest/job-group.service";
import {SharedModule} from "../shared/shared.module";

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
