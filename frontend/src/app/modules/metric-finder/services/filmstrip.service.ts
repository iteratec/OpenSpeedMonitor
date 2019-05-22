import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject} from 'rxjs';
import {Thumbnail} from '../models/thumbnail.model';
import {map} from 'rxjs/operators';
import {WptResultDTO} from '../models/wptResult-dto.model';
import {TestInfo, TestResult, TimingsMap} from '../models/test-result.model';
import {FilmstripView, Timing} from '../models/filmstrip-view.model';

@Injectable()
export class FilmstripService {

  filmStripData$ = new BehaviorSubject<{[resultId: string]: Thumbnail[]}>({});
  private viewInterval = 100;

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

  createFilmstripView(thumbnails: Thumbnail[], timings: TimingsMap, highlightedMetric?: string): FilmstripView {
    const end = Math.max(...thumbnails.map(t => t.time));
    const filmstrip = [];
    let lastVideoFrame = null;

    for (let time = 0; time < end + this.viewInterval; time += this.viewInterval) {
      const videoFrame = this.findFrame(thumbnails, time);
      const timingsInFrame = this.findTimingsInInterval(timings, time - this.viewInterval, time);
      filmstrip.push({
        time: time,
        imageUrl: videoFrame.imageUrl,
        hasChange: !lastVideoFrame || lastVideoFrame.time !== videoFrame.time,
        isHighlighted: !!timingsInFrame.find(timing => timing.metric === highlightedMetric),
        timings: timingsInFrame
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

  private findTimingsInInterval(timings: TimingsMap, start: number, end: number): Timing[] {
    return Object.keys(timings)
      .filter(metric => timings[metric] > start && timings[metric] <= end)
      .map(metric => ({metric: metric, time: timings[metric]}));
  }
}
