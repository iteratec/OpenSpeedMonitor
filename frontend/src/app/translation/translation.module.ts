import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {TranslateHttpLoader} from '@ngx-translate/http-loader'
import {OsmLangService} from "./services/osm-lang.service";

// AoT requires an exported function for factories
export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, './static/i18n/', `.json`);
}

@NgModule({
  imports: [
    CommonModule,
    HttpClientModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: (createTranslateLoader),
        deps: [HttpClient]
      }
    })
  ],
  declarations: [],
  providers: [
    OsmLangService
  ],
  exports: [
    TranslateModule
  ]
})
export class TranslationModule {

  supportedLangs: string[] = ['en', 'de'];

  constructor(private translateService: TranslateService, private osmLangService: OsmLangService) {
    translateService.addLangs(this.supportedLangs);
    translateService.setDefaultLang('en');

    translateService.use(this.supportedLangs.includes(this.osmLangService.getOsmLang()) ? this.osmLangService.getOsmLang() : translateService.getDefaultLang());
  }
}
