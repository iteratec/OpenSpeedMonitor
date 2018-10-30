import {Component} from '@angular/core';

export const APP_COMPONENT_SELECTOR = 'osm-app';

@Component({
  selector: APP_COMPONENT_SELECTOR,
  template: '<router-outlet></router-outlet>',
  styleUrls: []
})
export class AppComponent {
  constructor() {
  }
}
