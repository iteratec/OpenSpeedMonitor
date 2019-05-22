import {Component, ElementRef, Input, OnChanges, SimpleChanges, ViewChild} from '@angular/core';
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

  @ViewChild('scrollContainer')
  scrollContainer: ElementRef;

  offsets: number[] = [];

  private maxThumbnailWidth: number;

  constructor(private filmstripService: FilmstripService) { }

  ngOnChanges(changes: SimpleChanges): void {
    this.computeFilmstripAlignment();
    this.maxThumbnailWidth = 0;
  }

  computeFilmstripAlignment() {
    const thumbnailTimes = this.results.map(result => this.filmstripService.getThumbnailTime(result.timings[this.highlightedMetric]));
    const maxThumbnailTime = Math.max(...thumbnailTimes);
    this.offsets = thumbnailTimes.map(thumbnailTime => maxThumbnailTime - thumbnailTime);
  }

  identifyResult(index: number, result: TestResult) {
    return result.id;
  }

  highlightLoaded(highlightedImage: HTMLElement) {
    const thumbnailWidth = highlightedImage.offsetWidth;
    if (thumbnailWidth <= this.maxThumbnailWidth) {
      return;
    }
    this.maxThumbnailWidth = thumbnailWidth;
    const scrollElement = this.scrollContainer.nativeElement;
    if (scrollElement) {
      scrollElement.scrollLeft = highlightedImage.offsetLeft - scrollElement.offsetWidth / 2 + thumbnailWidth / 2;
    }
  }
}
