import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PageComparisonComponent} from './page-comparison.component';
import {HttpClientModule} from "@angular/common/http";
import {PageComparisonRowComponent} from "./page-comparison-row/page-comparison-row.component";
import {JobGroupService} from "../setup-dashboard/service/rest/job-group.service";
import {FormsModule} from '@angular/forms';
import {PageComparisonAdapterComponent} from "./adapter/page-comparison-adapter.component";

@NgModule({
  imports: [
    CommonModule, HttpClientModule, FormsModule
  ],
  declarations: [PageComparisonComponent, PageComparisonRowComponent, PageComparisonAdapterComponent],
  providers: [
    {provide: 'components', useValue: [PageComparisonAdapterComponent], multi: true}, JobGroupService
  ],
  entryComponents: [PageComparisonAdapterComponent]
})
export class PageComparisonModule { }
