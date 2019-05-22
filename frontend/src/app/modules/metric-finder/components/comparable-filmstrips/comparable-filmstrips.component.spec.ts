import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ComparableFilmstripsComponent } from './comparable-filmstrips.component';
import {FilmstripComponent} from '../filmstrip/filmstrip.component';
import {FilmstripService} from '../../services/filmstrip.service';
import {FilmstripServiceMock} from '../../services/filmstrip.service.mock';

describe('ComparableFilmstripsComponent', () => {
  let component: ComparableFilmstripsComponent;
  let fixture: ComponentFixture<ComparableFilmstripsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ComparableFilmstripsComponent, FilmstripComponent ],
      providers: [
        {provide: FilmstripService, useClass: FilmstripServiceMock}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ComparableFilmstripsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
