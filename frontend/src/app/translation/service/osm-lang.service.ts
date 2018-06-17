import {Injectable} from '@angular/core';
import {WindowRefService} from "../../common/service/window-ref.service";

@Injectable({
  providedIn: 'root'
})
export class OsmLangService {

  constructor(private winRef: WindowRefService) {
  }

  getOsmLang(): string {
    return this.winRef.getNativeWindow().OpenSpeedMonitor.i18n.lang;
  }
}
