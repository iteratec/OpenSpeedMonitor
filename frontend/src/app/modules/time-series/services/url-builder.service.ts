import {Injectable} from '@angular/core';
import {WptInfo} from '../models/wpt-info.model';

@Injectable({
  providedIn: 'root'
})
export class UrlBuilderService {

  options = {
    waterfall: new UrlOption('details', 'waterfall_view_step'),
    performanceReview: new UrlOption('performance_optimization', 'review_step'),
    contentBreakdown: new UrlOption('breakdown', 'breakdown_fv_step'),
    domains: new UrlOption('domains', 'breakdown_fv_step'),
    screenshot: new UrlOption('screen_shot', 'step_'),
  };

  buildSummaryUrl(wptInfo: WptInfo): string {
    return `${wptInfo.baseUrl}result/${wptInfo.testId}/#run${wptInfo.runNumber}_step${wptInfo.indexInJourney}`;
  }

  buildUrlByOption(wptInfo: WptInfo, option: UrlOption): string {
    return `${wptInfo.baseUrl}result/${wptInfo.testId}/${wptInfo.runNumber}/${option.pathArgName}/#${option.stepArgName}${wptInfo.indexInJourney}`;
  }

  buildFilmstripUrl(wptInfo: WptInfo): string {
    const wptInfoAsTestUrlData = this.wptInfoToTestUrlData(wptInfo);
    return `${wptInfo.baseUrl}video/compare.php?tests=${wptInfoAsTestUrlData}&ival=100&end=full&sticky=true`;
  }

  buildFilmstripToolUrl(wptInfo: WptInfo): string {
    const filmstripUrl = `${wptInfo.baseUrl}&testId=${wptInfo.testId}&view=filmstrip&step=${wptInfo.indexInJourney}`;
    return `https://iteratec.github.io/wpt-filmstrip/#wptUrl=${filmstripUrl}`;
  }

  buildFilmstripsComparisionUrl(wptInfos: WptInfo[]): string {
    // baseUrl for every point must be the same
    const baseUrl = wptInfos[0].baseUrl;

    const testsDataString = wptInfos.map((info: WptInfo) => this.wptInfoToTestUrlData(info)).join(',');
    return `${baseUrl}video/compare.php?tests=${testsDataString}&ival=100&end=full&sticky=true`;
  }

  private wptInfoToTestUrlData(wptInfo: WptInfo): string {
    return `${wptInfo.testId}-r:${wptInfo.runNumber}-c:0-s:${wptInfo.indexInJourney}`;
  }
}

class UrlOption {
  pathArgName: string;
  stepArgName: string;

  constructor(pathArgName: string, stepArgName: string) {
    this.pathArgName = pathArgName;
    this.stepArgName = stepArgName;
  }
}
