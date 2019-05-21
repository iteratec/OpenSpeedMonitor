import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {FilmstripService} from '../../services/filmstrip.service';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {Thumbnail} from '../../models/thumbnail.model';
import {distinctUntilChanged, filter, map, pluck} from 'rxjs/operators';
import {TestResult} from '../../models/test-result';
import {combineLatest} from 'rxjs/internal/observable/combineLatest';

@Component({
  selector: 'osm-filmstrip',
  templateUrl: './filmstrip.component.html',
  styleUrls: ['./filmstrip.component.scss']
})
export class FilmstripComponent implements OnChanges {

  filmStrip$: Observable<Thumbnail[]>;

  @Input()
  result: TestResult;

  private result$ = new BehaviorSubject<TestResult>(null);

  constructor(
    private filmstripService: FilmstripService
  ) {
    this.filmStrip$ = combineLatest(
      this.filmstripService.filmStripData$,
      this.result$
    ).pipe(
      map(([filmstrips, result]) => filmstrips[result.id]),
      filter(filmstrip => !!filmstrip),
      distinctUntilChanged(),
      map(filmstrip => this.filmstripService.createFilmStrip(100, filmstrip))
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['result'].currentValue !== changes['result'].previousValue) {
      this.filmstripService.loadFilmstripIfNecessary(this.result);
      this.result$.next(this.result);
    }
  }
}
