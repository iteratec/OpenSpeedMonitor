import { TestBed } from '@angular/core/testing';

import { MetricFinderService } from './metric-finder.service';
import { HttpTestingController, TestRequest} from '@angular/common/http/testing';
import {skip} from 'rxjs/operators';
import { TestResult, TestResultDTO} from '../models/test-result.model';
import {SharedMocksModule} from '../../../testing/shared-mocks.module';

describe('MetricFinderService', () => {
  let httpMock: HttpTestingController;
  let metricService: MetricFinderService;
  const now = Date.now();
  const testResultDto: TestResultDTO = {
    testInfo: {testId: '1', run: 1, step: 2, cached: false, wptUrl: 'http://wpt'},
    timings: {speedIndex: 5},
    date: new Date(now).toISOString()
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ SharedMocksModule ],
      providers: [ MetricFinderService ],
    });
    httpMock = TestBed.get(HttpTestingController);
    metricService = TestBed.get(MetricFinderService);
  });

  it('should put request data into observable', (done) => {
    metricService.testResults$.pipe(skip(1)).subscribe(result => {
      expect(result).toEqual([new TestResult(testResultDto)]);
      done();
    });
    metricService.loadData(new Date(now - 1000), new Date(now), 5, 6, 7);

    const mockRequest: TestRequest = httpMock.expectOne(req =>
      req.method === 'GET' && req.url === '/metricFinder/rest/getEventResults'
    );
    mockRequest.flush([testResultDto]);
    expect(mockRequest.request.params.get('from')).toEqual(new Date(now - 1000).toISOString());
    expect(mockRequest.request.params.get('to')).toEqual(new Date(now).toISOString());
    expect(mockRequest.request.params.get('applicationId')).toEqual('5');
    expect(mockRequest.request.params.get('pageId')).toEqual('6');
    expect(mockRequest.request.params.get('browserId')).toEqual('7');

    httpMock.verify();
  });
});
