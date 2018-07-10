import {Injectable} from '@angular/core';
import {GrailsBridgeService} from "../../shared/services/grails-bridge.service";

@Injectable()
export class OsmLangService {

  constructor(private grailsBridgeService: GrailsBridgeService) {
  }

  getOsmLang(): string {
    return this.grailsBridgeService.globalOsmNamespace.i18n.lang;
  }
}
