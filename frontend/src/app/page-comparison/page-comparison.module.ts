import {NgModule} from '@angular/core';
import {PageComparisonComponent} from './page-comparison.component';
import {SharedModule} from "../shared/shared.module";
import {JobGroupService} from "./services/job-group.service";
import {PageComparisonRowComponent} from "./components/page-comparison-row/page-comparison-row.component";

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
