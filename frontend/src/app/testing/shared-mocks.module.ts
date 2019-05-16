import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {NgModule} from '@angular/core';
import {TranslateTestLoader} from './ngx-translate-mocks';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {FormsModule} from "@angular/forms";
import {NgxSmartModalModule} from "ngx-smart-modal";
import {OwlDateTimeModule, OwlNativeDateTimeModule} from "ng-pick-datetime";
import {NgSelectModule} from "@ng-select/ng-select";


@NgModule({
  imports: [
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useClass: TranslateTestLoader
      }
    }),
    FormsModule,
    NgSelectModule,
    HttpClientTestingModule,
    RouterTestingModule,
    NgxSmartModalModule.forRoot(),
    OwlDateTimeModule,
    OwlNativeDateTimeModule
  ],
  exports: [
    TranslateModule,
    HttpClientTestingModule,
    RouterTestingModule,
    FormsModule,
    NgSelectModule,
    NgxSmartModalModule,
    OwlDateTimeModule,
    OwlNativeDateTimeModule
  ]
})
export class SharedMocksModule {

}
