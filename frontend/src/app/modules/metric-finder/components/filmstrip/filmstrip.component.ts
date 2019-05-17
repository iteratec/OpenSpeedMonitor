import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {FilmstripService} from '../../services/filmstrip.service';
import {BehaviorSubject, Observable} from 'rxjs';
import {Thumbnail} from '../../models/thumbnail.model';
import {distinctUntilChanged, filter, map, pluck} from 'rxjs/operators';
import {TestResult} from '../../models/test-result';

@Component({
  selector: 'osm-filmstrip',
  templateUrl: './filmstrip.component.html',
  styleUrls: ['./filmstrip.component.scss']
})
export class FilmstripComponent implements OnChanges{

  filmStrip$: Observable<Thumbnail[]>;

  @Input()
  result: TestResult;

  constructor(
    private filmstripService: FilmstripService
  ) {
    this.filmStrip$ = this.filmstripService.filmStripData$.pipe(
      map(filmstrips => filmstrips[this.result.id]),
      filter(filmstrip => !!filmstrip),
      distinctUntilChanged(),
      map(filmstrip => this.filmstripService.createFilmStrip(100, filmstrip))
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.filmstripService.loadFilmstripIfNecessary(this.result);
  }
}
