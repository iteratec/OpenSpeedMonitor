import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpClientModule} from "@angular/common/http";
import {FormsModule} from "@angular/forms";
import {JobGroupService} from "./service/rest/job-group.service";
import {TranslationModule} from "../translation/translation.module";
import {GrailsBridgeService} from "./service/grails-bridge.service";

@NgModule({
  imports: [
    CommonModule, HttpClientModule, FormsModule, TranslationModule
  ],
  declarations: [],
  providers: [
    JobGroupService,
    GrailsBridgeService
  ],
  exports: [
    CommonModule, HttpClientModule, FormsModule
  ]
})
export class SharedModule {
}
