import {TestBed} from '@angular/core/testing';

import {OsmLangService} from './osm-lang.service';
import {GrailsBridgeService} from "./grails-bridge.service";
import {RouterTestingModule} from "@angular/router/testing";

describe('OsmLangService', () => {

  let osmLangService: OsmLangService;
  let grailsBridgeService: GrailsBridgeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule
      ],
      providers: [
        OsmLangService,
        GrailsBridgeService
      ]
    });
    osmLangService = TestBed.get(OsmLangService);
    grailsBridgeService = TestBed.get(GrailsBridgeService)
  });

  it('should provide language set in global OpenSpeedMonitor namespace (from grails)', () => {
    grailsBridgeService.globalOsmNamespace = {i18n: {lang: 'de'}, user: {loggedIn: false}};
    expect(osmLangService.getOsmLang()).toBe('de');

    grailsBridgeService.globalOsmNamespace = {i18n: {lang: 'en'}, user: {loggedIn: false}};
    expect(osmLangService.getOsmLang()).toBe('en');
  });
});
