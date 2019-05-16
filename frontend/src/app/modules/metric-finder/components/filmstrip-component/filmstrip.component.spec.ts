import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {FilmstripComponent} from './filmstrip.component';
import {FilmstripService} from '../../services/filmstrip.service';
import {FilmstripServiceMock} from '../../services/filmstrip.service.mock';

describe('FilmstripComponent', () => {
  let component: FilmstripComponent;
  let fixture: ComponentFixture<FilmstripComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [FilmstripComponent],
      providers: [{
        provide: FilmstripService,
        useClass: FilmstripServiceMock,
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
