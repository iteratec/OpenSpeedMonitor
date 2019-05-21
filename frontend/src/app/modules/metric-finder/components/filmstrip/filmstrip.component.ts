import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {FilmstripService} from '../../services/filmstrip.service';
import {BehaviorSubject, Observable} from 'rxjs';
import {distinctUntilChanged, filter, map} from 'rxjs/operators';
import {TestResult} from '../../models/test-result';
import {combineLatest} from 'rxjs/internal/observable/combineLatest';
import {FilmstripView, Timing} from '../../models/filmstrip-view.model';
import {TranslateService} from '@ngx-translate/core';


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

  private result$ = new BehaviorSubject<TestResult>(null);

  constructor(
    private filmstripService: FilmstripService,
    private translationService: TranslateService
  ) {
    this.filmStrip$ = combineLatest(
      this.filmstripService.filmStripData$,
      this.result$
    ).pipe(
      map(([filmstrips, result]) => result ? filmstrips[result.id] : null),
      filter(filmstrip => !!filmstrip),
      distinctUntilChanged(),
      map(filmstrip => this.filmstripService.createFilmstripView(filmstrip, this.result.timings, this.highlightedMetric))
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['result'].currentValue !== changes['result'].previousValue) {
      this.filmstripService.loadFilmstripIfNecessary(this.result);
      this.result$.next(this.result);
    }
  }

  formatTime(millisecs: number, precision: number): string {
    return (millisecs / 1000).toFixed(precision) + 's';
  }

  formatTiming(timing: Timing): string {
    return this.getMetricName(timing.metric) + ': ' + this.formatTime(timing.time, 3);
  }

  getMetricName(metric: string): string {
    const prefixes = {
      _HERO_: 'Hero ',
      _UTME_: 'User Timing ',
      _UTMK_: 'User Timing Measure '
    };
    const matchingPrefix = Object.keys(prefixes).find(prefix => metric.startsWith(prefix));
    if (matchingPrefix) {
      return prefixes[matchingPrefix] + metric.substr(matchingPrefix.length);
    } else {
      return this.translationService.instant('frontend.de.iteratec.isr.measurand.' + metric);
    }
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
}
