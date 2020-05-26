import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {FilmstripService} from '../../services/filmstrip.service';
import {BehaviorSubject, Observable} from 'rxjs';
import {TestResult} from '../../models/test-result.model';
import {filter, map} from 'rxjs/operators';
import {combineLatest} from 'rxjs/internal/observable/combineLatest';
import {FilmstripView, Timing} from '../../models/filmstrip-view.model';
import {MetricFinderService} from '../../services/metric-finder.service';


@Component({
  selector: 'osm-filmstrip',
  templateUrl: './filmstrip.component.html',
  styleUrls: ['./filmstrip.component.scss']
})
export class FilmstripComponent implements OnChanges {

  filmStrip$: Observable<FilmstripView>;

  @Input()
  result: TestResult;

  @Input()
  highlightedMetric: string;

  @Input()
  offset: number;

  @Output()
  highlightLoad = new EventEmitter<HTMLElement>();

  private result$ = new BehaviorSubject<TestResult>(null);

  constructor(
    private filmstripService: FilmstripService,
    private metricFinderService: MetricFinderService
  ) {
    this.filmStrip$ = combineLatest(
      this.filmstripService.filmStripData$,
      this.result$
    ).pipe(
      map(([filmstrips, result]) => result ? filmstrips[result.id] : null),
      filter(filmstrip => !!filmstrip),
      map(filmstrip => this.filmstripService.createFilmstripView(filmstrip, this.result.timings, this.highlightedMetric, this.offset))
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['result'] && changes['result'].currentValue !== changes['result'].previousValue) {
      this.filmstripService.loadFilmstripIfNecessary(this.result);
    }
    this.result$.next(this.result);
  }

  formatTime(millisecs: number, precision: number): string {
    return (millisecs / 1000).toFixed(precision) + 's';
  }

  formatTiming(timing: Timing): string {
    return this.metricFinderService.getMetricName(timing.metric) + ': ' + this.formatTime(timing.time, 3);
  }

  positionTimings(event: MouseEvent) {
    const container = event.currentTarget as HTMLElement;
    const timings = container ? container.querySelector('.timings') as HTMLElement : null;
    if (timings) {
      const offsetLeft = this.getOffsetLeft(container);
      timings.style.top = container.offsetTop + container.offsetHeight + 'px';
      timings.style.left = offsetLeft + 'px';
      timings.style.minWidth = container.getBoundingClientRect().width + 'px';
    }
  }

  private getOffsetLeft(element: HTMLElement) {
    const offsetParent = element.offsetParent;
    let offsetLeft = element.offsetLeft;
    for (let parent = element.parentElement; parent !== offsetParent; parent = parent.parentElement) {
      offsetLeft -= parent.scrollLeft;
    }
    return offsetLeft;
  }

  highlightLoaded(event: Event) {
    this.highlightLoad.emit(event.target as HTMLElement);
  }
}
