import {Component} from '@angular/core';
import {FilmstripService} from '../../services/filmstrip.service';
import {BehaviorSubject, Observable} from 'rxjs';
import {Thumbnail} from '../../models/thumbnail.model';
import {map} from 'rxjs/operators';

@Component({
  selector: 'osm-filmstrip',
  templateUrl: './filmstrip.component.html',
  styleUrls: ['./filmstrip.component.scss']
})
export class FilmstripComponent {
  filmStripData$: BehaviorSubject<Thumbnail[]>;
  filmStrip$: Observable<Thumbnail[]>;

  constructor(private filmstripService: FilmstripService) {
    this.filmstripService.getFilmstripData();
    this.filmStripData$ = this.filmstripService.filmStripData$;

    this.filmStrip$ = this.filmStripData$.pipe(map(value => this.filmstripService.createFilmStrip(100, value)));
  }
}
