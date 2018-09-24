import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {FormsModule} from "@angular/forms";
import {GrailsBridgeService} from "./services/grails-bridge.service";
import {TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {OsmLangService} from "./services/osm-lang.service";


// AoT requires an exported function for factories
export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, '/static/i18n/', `.json`);
}

@NgModule({
  imports: [
    CommonModule,
    HttpClientModule,
    FormsModule,
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
    GrailsBridgeService,
    OsmLangService
  ],
  exports: [
    CommonModule,
    HttpClientModule,
    FormsModule,
    TranslateModule
  ]
})
export class SharedModule {
  supportedLangs: string[] = ['en', 'de'];

  constructor(private osmLangService: OsmLangService, private translateService: TranslateService) {
    translateService.addLangs(this.supportedLangs);
    translateService.setDefaultLang('en');

    translateService.use(this.supportedLangs.includes(this.osmLangService.getOsmLang()) ? this.osmLangService.getOsmLang() : translateService.getDefaultLang());
  }
}
