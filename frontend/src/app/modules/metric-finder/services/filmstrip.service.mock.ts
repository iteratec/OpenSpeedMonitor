import {BehaviorSubject} from 'rxjs';
import {Thumbnail} from '../models/thumbnail.model';
import {TestResult} from '../models/test-result.model';

export class FilmstripServiceMock {

  filmStripData$ = new BehaviorSubject<{[resultId: string]: Thumbnail[]}>({});

  loadFilmstripIfNecessary(result: TestResult): void {}

  loadFilmstrip(result: TestResult): void { }

  getThumbnailTime(time: number): number { return 0; }
}
