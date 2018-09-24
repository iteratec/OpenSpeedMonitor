import {Injectable} from '@angular/core';
import {GlobalOsmNamespace} from "../models/global-osm-namespace.model";

@Injectable()
export class GrailsBridgeService {

  private nativeWindow: any = window;
  globalOsmNamespace: GlobalOsmNamespace = this.nativeWindow.OpenSpeedMonitor;

  constructor() {
  }
}
