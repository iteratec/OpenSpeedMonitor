import { TestBed, inject } from '@angular/core/testing';

import { OsmLangService } from './osm-lang.service';

describe('OsmLangService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [OsmLangService]
    });
  });

  it('should be created', inject([OsmLangService], (service: OsmLangService) => {
    expect(service).toBeTruthy();
  }));
});
