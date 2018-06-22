import {NgModule} from '@angular/core';
import {PageComparisonComponent} from './page-comparison.component';
import {PageComparisonRowComponent} from "./page-comparison-row/page-comparison-row.component";
import {JobGroupService} from "../shared/service/rest/job-group.service";
import {PageComparisonAdapterComponent} from "./adapter/page-comparison-adapter.component";
import {SharedModule} from "../shared/shared.module";

@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [PageComparisonComponent, PageComparisonRowComponent, PageComparisonAdapterComponent],
  providers: [
    {provide: 'components', useValue: [PageComparisonAdapterComponent], multi: true}, JobGroupService
  ],
  entryComponents: [PageComparisonAdapterComponent]
})
export class PageComparisonModule { }
