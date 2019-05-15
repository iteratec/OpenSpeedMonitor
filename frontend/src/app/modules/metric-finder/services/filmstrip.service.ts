import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject} from 'rxjs';
import {Thumbnail, ThumbnailDto} from '../models/thumbnail.model';
import {map} from 'rxjs/operators';
import {WptResultDto} from '../models/wptResult-dto.model';

@Injectable()
export class FilmstripService {

  // https://prod.server01.wpt.iteratec.de/video/compare.php?tests=190426_6D_94880eef447fa99fdb40ea094eef00fa-r:1-c:0-s:4&ival=100&end=full
  // tslint:disable-next-line:max-line-length
  filmStripDataUrl = 'https://prod.server01.wpt.iteratec.de/result/190423_CS_dcc38f026923b7e3fde79b617e360475/?f=json&average=0&median=0&standard=0&requests=0&console=0&multistepFormat=1';

  filmStripData$: BehaviorSubject<ThumbnailDto[]> = new BehaviorSubject<ThumbnailDto[]>([]);

  constructor (private http: HttpClient) {}

  getFilmstrip() {
    return this.http.get<WptResultDto>(this.filmStripDataUrl).pipe(
      map( (wptData: WptResultDto) => (wptData.data.runs[1].firstView.steps[0].videoFrames)
        .map(item => new Thumbnail(item.time, item.image))))
      .subscribe(value => this.filmStripData$.next(value),
          error => this.handleError(error));
  }

  getThumbnails(): any {
    return null;
  }

  private handleError(error: any) {
    console.log(error);
  }
}
