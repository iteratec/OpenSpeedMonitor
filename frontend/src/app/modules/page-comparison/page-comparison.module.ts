import {NgModule} from '@angular/core';
import {PageComparisonComponent} from './page-comparison.component';
import {JobGroupService} from "./services/job-group.service";
import {PageComparisonRowComponent} from "./components/page-comparison-row/page-comparison-row.component";
import {SharedModule} from "../shared.module";
import {HttpClientModule} from "@angular/common/http";
import {FormsModule} from "@angular/forms";

@NgModule({
  imports: [
    SharedModule,
    HttpClientModule,
    FormsModule
  ],
  declarations: [PageComparisonComponent, PageComparisonRowComponent],
  providers: [
    {provide: 'components', useValue: [PageComparisonComponent], multi: true}, JobGroupService
  ],
  entryComponents: [PageComparisonComponent]
})
export class PageComparisonModule {
}
