import {BehaviorSubject} from 'rxjs';
import {Thumbnail} from '../models/thumbnail.model';

export class FilmstripServiceMock {

  filmStripData$ = new BehaviorSubject<Thumbnail[]>([]);

  getFilmstripData() {}

  createFilmStrip() {}
}
