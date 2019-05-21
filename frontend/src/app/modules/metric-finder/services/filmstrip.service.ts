import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject} from 'rxjs';
import {Thumbnail, ThumbnailDto} from '../models/thumbnail.model';
import {map} from 'rxjs/operators';
import {WptResultDTO} from '../models/wptResult-dto.model';
import {TestInfo, TestResult} from '../models/test-result';
import {FilmstripView} from '../models/filmstrip-view.model';

@Injectable()
export class FilmstripService {

  filmStripData$ = new BehaviorSubject<{[resultId: number]: Thumbnail[]}>({});

  constructor (private http: HttpClient) {}

  loadFilmstripIfNecessary(result: TestResult): void {
    const loadedResultIds = Object.keys(this.filmStripData$.getValue());
    if (loadedResultIds.indexOf(result.id) < 0) {
      this.loadFilmstrip(result);
    }
  }

  loadFilmstrip(result: TestResult): void {
    const wptUrl = this.createWptUrl(result.testInfo);
    this.http.get<WptResultDTO>(wptUrl).pipe(
      map( (wptData: WptResultDTO) => this.extractFilmstrip(wptData, result.testInfo))
    ).subscribe(
      filmstrip => this.updateFilmstripData(result, filmstrip),
      error => this.handleError(error)
    );
  }

  createFilmstripView(interval: number, thumbnails: Thumbnail[], highlightedTime?: number): FilmstripView {
    const end = Math.max(...thumbnails.map(t => t.time));
    const filmstrip = [];
    let lastVideoFrame = null;

    for (let time = 0; time < end + interval; time += interval) {
      const videoFrame = this.findFrame(thumbnails, time);
      filmstrip.push({
        time: time,
        imageUrl: videoFrame.imageUrl,
        hasChange: !lastVideoFrame || lastVideoFrame.time !== videoFrame.time,
        isHighlighted: highlightedTime !== undefined && highlightedTime > (time - interval) && highlightedTime <= time
      });
      lastVideoFrame = videoFrame;
    }
    return filmstrip;
  }

  private findFrame(thumbnails, time) {
    let frame = thumbnails[0];
    for (const currentFrame of thumbnails) {
      if (time >= currentFrame.time) {
        frame = currentFrame;
      } else {
        break;
      }
    }
    return frame;
  }

  private updateFilmstripData(result: TestResult, filmstrip: Thumbnail[]): void {
    this.filmStripData$.next({
      ...this.filmStripData$.getValue(),
      [result.id]: filmstrip
    });
  }

  private extractFilmstrip(wptResult: WptResultDTO, testInfo: TestInfo): Thumbnail[] {
    const cachedView = testInfo.cached ? 'repeatedView' : 'firstView';
    const frames = wptResult.data.runs[testInfo.run][cachedView].steps[testInfo.step - 1].videoFrames;
    return frames.map(item => new Thumbnail(item.time, item.image));
  }


  private createWptUrl(testInfo: TestInfo): string {
    return `${testInfo.wptUrl}/result/${testInfo.testId}/?f=json&average=0&median=0&standard=0&requests=0&console=0&multistepFormat=1`;
  }

  private handleError(error: any) {
    console.error(error);
  }
}
