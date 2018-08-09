import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {NgModule} from '@angular/core';
import {TranslateTestLoader} from './ngx-translate-mocks';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';


@NgModule({
  imports: [
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useClass: TranslateTestLoader
      }
    }),
    HttpClientTestingModule,
    RouterTestingModule,
  ],
  exports: [
    TranslateModule,
    HttpClientTestingModule,
    RouterTestingModule
  ]
})
export class SharedMocksModule {

}
