import {Injectable} from '@angular/core';
import {WptInfo} from '../models/wpt-info.model';

@Injectable({
  providedIn: 'root'
})
export class UrlBuilderService {

  private _urlOptions: { [key: string]: UrlOption } = {
    waterfall: new UrlOption('details', 'waterfall_view_step'),
    performanceReview: new UrlOption('performance_optimization', 'review_step'),
    contentBreakdown: new UrlOption('breakdown', 'breakdown_fv_step'),
    domains: new UrlOption('domains', 'breakdown_fv_step'),
    screenshot: new UrlOption('screen_shot', 'step_'),
  };

  private static buildUrlByOption(wptInfo: WptInfo, option: UrlOption): string {
    const path = `result/${wptInfo.testId}/${wptInfo.runNumber}/${option.pathArgName}`;
    const fragment = `#${option.stepArgName}${wptInfo.indexInJourney}`;

    return `${wptInfo.baseUrl}${path}/${fragment}`;
  }

  private static wptInfoToTestUrlData(wptInfo: WptInfo): string {
    return `${wptInfo.testId}-r:${wptInfo.runNumber}-c:0-s:${wptInfo.indexInJourney}`;
  }

  buildSummaryUrl(wptInfo: WptInfo): string {
    return `${wptInfo.baseUrl}result/${wptInfo.testId}/#run${wptInfo.runNumber}_step${wptInfo.indexInJourney}`;
  }

  buildWaterfallUrl(wptInfo: WptInfo): string {
    return UrlBuilderService.buildUrlByOption(wptInfo, this._urlOptions.waterfall);
  }

  buildPerformanceReviewUrl(wptInfo: WptInfo): string {
    return UrlBuilderService.buildUrlByOption(wptInfo, this._urlOptions.performanceReview);
  }

  buildContentBreakdownUrl(wptInfo: WptInfo): string {
    return UrlBuilderService.buildUrlByOption(wptInfo, this._urlOptions.contentBreakdown);
  }

  buildDomainsUrl(wptInfo: WptInfo): string {
    return UrlBuilderService.buildUrlByOption(wptInfo, this._urlOptions.domains);
  }

  buildScreenshotUrl(wptInfo: WptInfo): string {
    return UrlBuilderService.buildUrlByOption(wptInfo, this._urlOptions.screenshot);
  }

  buildFilmstripUrl(wptInfo: WptInfo): string {
    const wptInfoAsTestUrlData = UrlBuilderService.wptInfoToTestUrlData(wptInfo);
    return `${wptInfo.baseUrl}video/compare.php?tests=${wptInfoAsTestUrlData}&ival=100&end=full&sticky=true`;
  }

  buildFilmstripToolUrl(wptInfo: WptInfo): string {
    const filmstripUrl = `${wptInfo.baseUrl}&testId=${wptInfo.testId}&view=filmstrip&step=${wptInfo.indexInJourney}`;
    return `https://iteratec.github.io/wpt-filmstrip/#wptUrl=${filmstripUrl}`;
  }

  buildFilmstripsComparisionUrl(wptInfos: WptInfo[]): string {
    // baseUrl for every point must be the same
    const baseUrl = wptInfos[0].baseUrl;

    const testsDataString = wptInfos.map((info: WptInfo) => UrlBuilderService.wptInfoToTestUrlData(info)).join(',');
    return `${baseUrl}video/compare.php?tests=${testsDataString}&ival=100&end=full&sticky=true`;
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
