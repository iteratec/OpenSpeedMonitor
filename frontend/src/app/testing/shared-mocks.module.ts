import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {NgModule} from '@angular/core';
import {TranslateTestLoader} from './ngx-translate-mocks';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {FormsModule} from "@angular/forms";


@NgModule({
  imports: [
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useClass: TranslateTestLoader
      }
    }),
    FormsModule,
    HttpClientTestingModule,
    RouterTestingModule,
  ],
  exports: [
    TranslateModule,
    HttpClientTestingModule,
    RouterTestingModule,
    FormsModule
  ]
})
export class SharedMocksModule {

}
