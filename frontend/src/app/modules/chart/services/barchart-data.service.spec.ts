import { TestBed } from '@angular/core/testing';
import {BarchartDataService} from "./barchart-data.service";

describe('BarchartDataService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: BarchartDataService = TestBed.get(BarchartDataService);
    expect(service).toBeTruthy();
  });
});
