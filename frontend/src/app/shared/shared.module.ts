import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpClientModule} from "@angular/common/http";
import {FormsModule} from "@angular/forms";
import {JobGroupService} from "./service/rest/job-group.service";

@NgModule({
  imports: [
    CommonModule, HttpClientModule, FormsModule
  ],
  declarations: [],
  providers: [
    JobGroupService
  ],
  exports: [
    CommonModule, HttpClientModule, FormsModule
  ]
})
export class SharedModule {
}
