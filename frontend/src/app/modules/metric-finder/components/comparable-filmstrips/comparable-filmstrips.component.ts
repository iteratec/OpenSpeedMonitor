import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {TestResult} from '../../models/test-result';
import {FilmstripService} from '../../services/filmstrip.service';

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

  offsets: number[] = [];

  constructor(private filmstripService: FilmstripService) { }

  ngOnChanges(changes: SimpleChanges): void {
    this.computeFilmstripAlignment();
  }

  computeFilmstripAlignment() {
    const thumbnailTimes = this.results.map(result => this.filmstripService.getThumbnailTime(result.timings[this.highlightedMetric]));
    const maxThumbnailTime = Math.max(...thumbnailTimes);
    this.offsets = thumbnailTimes.map(thumbnailTime => maxThumbnailTime - thumbnailTime);
  }

}
