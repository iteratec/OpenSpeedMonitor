import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {TestResult} from '../../models/test-result';

@Component({
  selector: 'osm-comparable-filmstrips',
  templateUrl: './comparable-filmstrips.component.html',
  styleUrls: ['./comparable-filmstrips.component.scss']
})
export class ComparableFilmstripsComponent implements OnChanges{

  @Input()
  results: TestResult[];

  @Input()
  highlightedMetric: string;

  constructor() { }

  ngOnChanges(changes: SimpleChanges): void {
  }

}
