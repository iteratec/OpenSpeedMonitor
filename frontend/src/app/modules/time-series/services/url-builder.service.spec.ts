import {TestBed} from '@angular/core/testing';

import {UrlBuilderService} from './url-builder.service';

describe('UrlBuilderService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: UrlBuilderService = TestBed.get(UrlBuilderService);
    expect(service).toBeTruthy();
  });
});
