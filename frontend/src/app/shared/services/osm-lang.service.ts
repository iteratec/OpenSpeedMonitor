import {Injectable} from "@angular/core";
import {GrailsBridgeService} from "./grails-bridge.service";

@Injectable()
export class OsmLangService {

  constructor(private grailsBridgeService: GrailsBridgeService) {
  }

  getOsmLang(): string {
    if (this.grailsBridgeService.globalOsmNamespace &&
      this.grailsBridgeService.globalOsmNamespace.i18n) {
      return this.grailsBridgeService.globalOsmNamespace.i18n.lang;
    } else {
      return undefined;
    }

  }
}
