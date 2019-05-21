import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController, TestRequest} from '@angular/common/http/testing';
import {skip} from 'rxjs/operators';
import {FilmstripService} from './filmstrip.service';
import {WptResultDTO} from '../models/wptResult-dto.model';
import {Thumbnail} from '../models/thumbnail.model';
import {TestResult} from '../models/test-result';

describe('FilmstripService', () => {
  let httpMock: HttpTestingController;
  let filmstripService: FilmstripService;

  const wptResultDTO: WptResultDTO = {
    data: {
      runs: {
        1: {
          firstView: {
            steps: [{
              videoFrames: [{
                time: 100,
                image: 'bild1'
              }, {
                time: 400,
                image: 'bild2'
              }]
            }]
          }
        }
      }
    }
  };

  const thumbnails: Thumbnail[] = [
    new Thumbnail(100, 'bild1'),
    new Thumbnail(400, 'bild2'),
  ];
  const testResult = new TestResult({
    testInfo: {wptUrl: 'http://wpt', testId: 'XX_YY', run: 1, cached: false, step: 1},
    date: new Date(),
    timings: {}
  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [FilmstripService]
    });
    httpMock = TestBed.get(HttpTestingController);
    filmstripService = TestBed.get(FilmstripService);
  });

  it('should put request data into observable', (done) => {
    filmstripService.filmStripData$.pipe(skip(1)).subscribe(result => {
      expect(result).toEqual({[testResult.id]: thumbnails});
      done();
    });
    filmstripService.loadFilmstrip(testResult);

    const mockRequest: TestRequest = httpMock.expectOne(req =>
      req.method === 'GET' && req.url.startsWith('http://wpt/result')
    );
    mockRequest.flush(wptResultDTO);
    httpMock.verify();
  });

  it('loadFilmstripIfNecessary doesnt load data again if already loaded', () => {
    filmstripService.loadFilmstripIfNecessary(testResult);
    const mockRequest: TestRequest = httpMock.expectOne(req =>
      req.method === 'GET' && req.url.startsWith('http://wpt/result')
    );
    mockRequest.flush(wptResultDTO);

    filmstripService.loadFilmstripIfNecessary(testResult);
    expect(httpMock.match(req => req.url.startsWith('http://wpt/result')).length).toEqual(0);
    httpMock.verify();
  });

  it('fill up filmstrip list with interval steps and thumbnails', () => {
    const calculatedFilmstrip: Thumbnail[] = filmstripService.createFilmstripView(100, thumbnails, 120);
    const expectedFilmstrip = [
      {time: 0, imageUrl: 'bild1', hasChange: true, isHighlighted: false},
      {time: 100, imageUrl: 'bild1', hasChange: false, isHighlighted: false},
      {time: 200, imageUrl: 'bild1', hasChange: false, isHighlighted: true},
      {time: 300, imageUrl: 'bild1', hasChange: false, isHighlighted: false},
      {time: 400, imageUrl: 'bild2', hasChange: true, isHighlighted: false}
    ];

    expect(calculatedFilmstrip).toEqual(expectedFilmstrip);
  });
});
