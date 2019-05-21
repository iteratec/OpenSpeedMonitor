import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {FilmstripService} from '../../services/filmstrip.service';
import {BehaviorSubject, Observable} from 'rxjs';
import {distinctUntilChanged, filter, map} from 'rxjs/operators';
import {TestResult} from '../../models/test-result';
import {combineLatest} from 'rxjs/internal/observable/combineLatest';
import {FilmstripView, FilmstripViewThumbnail} from '../../models/filmstrip-view.model';


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
    private filmstripService: FilmstripService
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
