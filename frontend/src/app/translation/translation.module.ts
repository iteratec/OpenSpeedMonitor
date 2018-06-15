import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {TranslateHttpLoader} from '@ngx-translate/http-loader'
import {OsmCommonModule} from "../common/osm.common.module";
import {WindowRefService} from "../common/service/window-ref.service";
import {OsmLangService} from "./service/osm-lang.service";

// AoT requires an exported function for factories
export function createTranslateLoader(http: HttpClient, windowRef: WindowRefService) {
  return new TranslateHttpLoader(http, './static/i18n/', `.json`);
}

@NgModule({
  imports: [
    CommonModule,
    OsmCommonModule,
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
  providers: [OsmLangService],
  exports: [
    CommonModule,
    TranslateModule
  ]
})
export class TranslationModule {

  constructor(private translate: TranslateService, private osmLangService: OsmLangService) {
    let supportedLangs: string[] = ['en', 'de'];
    translate.addLangs(supportedLangs);
    translate.setDefaultLang('en');

    translate.use(supportedLangs.includes(this.osmLangService.getOsmLang()) ? this.osmLangService.getOsmLang() : translate.getDefaultLang());
  }
}
