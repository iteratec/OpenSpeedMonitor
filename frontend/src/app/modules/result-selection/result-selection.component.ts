import {Component, ElementRef} from '@angular/core';

import {ResultSelectionService} from "./services/result-selection.service";

@Component({
  selector: 'osm-result-selection',
  templateUrl: './result-selection.component.html',
  styleUrls: ['./result-selection.component.scss']
})
export class ResultSelectionComponent {

  currentChart: string;


  constructor(private resultSelectionService: ResultSelectionService, element: ElementRef) {
    this.currentChart = element.nativeElement.getAttribute('data-current-chart');
  }

}
