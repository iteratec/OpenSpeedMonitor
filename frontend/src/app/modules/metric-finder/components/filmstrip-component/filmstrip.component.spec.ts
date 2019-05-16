import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {FilmstripComponent} from './filmstrip.component';
import {FilmstripService} from '../../services/filmstrip.service';
import {of} from 'rxjs';

describe('FilmstripComponent', () => {
  let component: FilmstripComponent;
  let fixture: ComponentFixture<FilmstripComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [FilmstripComponent],
      providers: [{
        provide: FilmstripService,
        useClass: class {
          public filmStripData$ = of([[]]);
          public getFilmstripData = jasmine.createSpy('getFilmstripData');
        }
      }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FilmstripComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    const filmstripService: FilmstripService = TestBed.get(FilmstripService);
    expect(component).toBeTruthy();
    expect(filmstripService.getFilmstripData).toHaveBeenCalled();
  });
});
