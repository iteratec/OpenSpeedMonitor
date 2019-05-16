import {FilmstripService} from './filmstrip.service';
import {BehaviorSubject, of} from 'rxjs';
import {Thumbnail} from '../models/thumbnail.model';
import {NgModule} from '@angular/core';

export class FilmstripServiceMock {

  filmStripData$ = new BehaviorSubject<Thumbnail[]>([]);

  getFilmstripData = jasmine.createSpy('getFilmstripData');

  createFilmStrip(interval: number, thumbnails: Thumbnail[]): any[] {
    return [];
  }
}
