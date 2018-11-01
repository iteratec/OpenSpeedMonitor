import {Component, Input} from '@angular/core';

@Component({
  selector: 'osm-empty-state',
  templateUrl: './empty-state.component.html',
  styleUrls: ['./empty-state.component.scss']
})
export class EmptyStateComponent {

  @Input()
  messageKey: string;

  @Input()
  image: string;

  constructor() {
  }
}
