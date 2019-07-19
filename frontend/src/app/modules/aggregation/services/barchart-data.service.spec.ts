import {TestBed} from '@angular/core/testing';
import {BarchartDataService} from "./barchart-data.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('BarchartDataService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [BarchartDataService],
    imports: [HttpClientTestingModule]
  }));

  it('should be created', () => {
    const service: BarchartDataService = TestBed.get(BarchartDataService);
    expect(service).toBeTruthy();
  });
});
