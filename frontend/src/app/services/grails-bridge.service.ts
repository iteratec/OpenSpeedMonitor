import {Injectable} from '@angular/core';
import {GlobalOsmNamespace} from "../models/global-osm-namespace.model";
import {NavigationEnd, Router} from "@angular/router";

@Injectable()
export class GrailsBridgeService {

  private nativeWindow: any = window;
  globalOsmNamespace: GlobalOsmNamespace = this.nativeWindow.OpenSpeedMonitor;

  constructor(private router: Router) {
    router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.toggleNavbarClasses(event.urlAfterRedirects, "data-active-matches", "active");
        this.toggleNavbarClasses(event.urlAfterRedirects, "data-open-matches", "open");
      }
    });
  }

  private toggleNavbarClasses(newUrl: string, attributeName: string, cssClass: string) {
    const matches = Array.from(document.querySelectorAll(`#main-navbar [${attributeName}]`))
      .filter(node => new RegExp(node.getAttribute(attributeName)).test(newUrl));
    if (matches.length) {
      Array.from(document.querySelectorAll(`#main-navbar .${cssClass}`))
        .forEach(node => node.classList.remove(cssClass));
      matches.forEach(node => node.classList.add(cssClass));
    }
  }
}
