import {BehaviorSubject} from 'rxjs';
import {Thumbnail} from '../models/thumbnail.model';
import {TestResult} from '../models/test-result';

export class FilmstripServiceMock {

  filmStripData$ = new BehaviorSubject<{[resultId: string]: Thumbnail[]}>({});

  loadFilmstripIfNecessary(result: TestResult): void {}

  loadFilmstrip(result: TestResult): void { }
}
