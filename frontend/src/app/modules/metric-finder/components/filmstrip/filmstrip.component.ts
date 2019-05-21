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
      map(filmstrip => this.filmstripService.createFilmstripView(100, filmstrip, this.result.timings[this.highlightedMetric]))
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['result'].currentValue !== changes['result'].previousValue) {
      this.filmstripService.loadFilmstripIfNecessary(this.result);
      this.result$.next(this.result);
    }
  }

  formatTime(thumbnail: FilmstripViewThumbnail): string {
    return (thumbnail.time / 1000).toFixed(1) + 's';
  }

}
