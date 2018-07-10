import { TranslationModule } from './translation.module';
import {TestBed} from "@angular/core/testing";
import {TranslateService} from "@ngx-translate/core";
import {OsmLangService} from "./services/osm-lang.service";

describe('TranslationModule', () => {
  let translationModule: TranslationModule;
  let osmLangServiceSpy = jasmine.createSpyObj('OsmLangService',
    ['getOsmLang']);
  let translateServiceSpy = jasmine.createSpyObj('TranslateService',
    ['getDefaultLang', 'setDefaultLang', 'use', 'addLangs']);

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {provide: TranslateService, useValue: translateServiceSpy},
        {provide: OsmLangService, useValue: osmLangServiceSpy}
      ]
    });
    translationModule = new TranslationModule(
      TestBed.get(TranslateService),
      TestBed.get(OsmLangService)
    )
  });

  it('sets en as default lang', () => {
    expect(getMostRecentCallsArgs(translateServiceSpy.setDefaultLang)).toEqual(['en']);
  });
  it('sets en and de as supported languages', () => {
    const supportedLangs: string[] = ['en', 'de'];
    expect(getMostRecentCallsArgs(translateServiceSpy.addLangs)).toEqual([supportedLangs]);

  });
  it('osm lang is used if it is within supported langs', () => {
    osmLangServiceSpy.getOsmLang.and.returnValue('de');
    translationModule = new TranslationModule(
      TestBed.get(TranslateService),
      TestBed.get(OsmLangService)
    );
    expect(getMostRecentCallsArgs(translateServiceSpy.use)).toEqual(['de']);
    osmLangServiceSpy.getOsmLang.and.returnValue('en');
    translationModule = new TranslationModule(
      TestBed.get(TranslateService),
      TestBed.get(OsmLangService)
    );
    expect(getMostRecentCallsArgs(translateServiceSpy.use)).toEqual(['en']);
  });
  it('default lang is used if osm lang is not supported', () => {
    osmLangServiceSpy.getOsmLang.and.returnValue('not_supported_lang');
    let defaultLang = 'en';
    translateServiceSpy.getDefaultLang.and.returnValue(defaultLang)
    translationModule = new TranslationModule(
      TestBed.get(TranslateService),
      TestBed.get(OsmLangService)
    );
    expect(getMostRecentCallsArgs(translateServiceSpy.use)).toEqual([defaultLang]);
  });
});

function getMostRecentCallsArgs(method){
  return method.calls.argsFor(method.calls.count()-1);
}
