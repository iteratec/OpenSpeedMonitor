import {Component} from '@angular/core';
import {FilmstripService} from '../../services/filmstrip.service';
import {BehaviorSubject} from 'rxjs';
import {ThumbnailDto} from '../../models/thumbnail.model';

@Component({
  selector: 'osm-filmstrip',
  templateUrl: './filmstrip.component.html',
  styleUrls: ['./filmstrip.component.scss']
})
export class FilmstripComponent {

  private filmstripService: FilmstripService;
  filmStripData$: BehaviorSubject<ThumbnailDto[]>;

  constructor(filmstripService: FilmstripService) {
    this.filmstripService = filmstripService;
    this.filmStripData$ = this.filmstripService.filmStripData$;
    this.filmstripService.getFilmstrip();
  }
}
